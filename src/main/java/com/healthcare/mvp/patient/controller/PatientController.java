package com.healthcare.mvp.patient.controller;

import com.healthcare.mvp.patient.dto.CreatePatientRequest;
import com.healthcare.mvp.patient.dto.PatientDto;
import com.healthcare.mvp.patient.service.PatientService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient Management", description = "Complete patient lifecycle management including registration, updates, and medical history")
@Validated
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientController {
    
    private final PatientService patientService;

    /**
     * Register new patient - Hospital Admin, Receptionist, or Doctor
     */
    @PostMapping
    @Operation(
        summary = "Register New Patient",
        description = "Register a new patient in the hospital system with complete medical information",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Patient registered successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data - check required fields"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Conflict - Patient with this MRN already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('RECEPTIONIST') or hasRole('DOCTOR')")
    public ResponseEntity<BaseResponse<PatientDto>> registerPatient(
            @Valid @RequestBody
            @Parameter(description = "Patient registration details", required = true)
            CreatePatientRequest request,
            Authentication authentication) {

        log.info("Registering new patient for hospital: {}", request.getHospitalId());

        try {
            PatientDto patient = patientService.registerPatient(request);

            return ResponseEntity.ok(BaseResponse.success(
                "Patient registered successfully with ID: " + patient.getGlobalPatientId(),
                patient
            ));

        } catch (Exception e) {
            log.error("Failed to register patient: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to register patient: "+ e.getMessage())
            );
        }
    }

    /**
     * Get Hospital Patients with Pagination and Search
     * FIXED: Combined both methods into one with optional search and pagination
     */
    @GetMapping("/hospital/{hospitalId}")
    @Operation(
        summary = "Get Hospital Patients",
        description = "Retrieve patients for a specific hospital with optional search and pagination",
        parameters = {
            @Parameter(name = "hospitalId", description = "Hospital UUID", required = true),
            @Parameter(name = "search", description = "Search term for patient name, email, or MRN"),
            @Parameter(name = "page", description = "Page number (0-based)"),
            @Parameter(name = "size", description = "Page size"),
            @Parameter(name = "sort", description = "Sort criteria")
        }
    )
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<Page<PatientDto>>> getHospitalPatients(
            @PathVariable UUID hospitalId,
            @RequestParam(required = false) String search,
            Pageable pageable,
            Authentication authentication) {

        log.info("Fetching patients for hospital: {} with search: {}", hospitalId, search);

        try {
            Page<PatientDto> patients;

            if (search != null && !search.trim().isEmpty()) {
                patients = patientService.searchPatientsByHospital(hospitalId, search.trim(), pageable);
            } else {
                patients = patientService.getPatientsByHospital(hospitalId, pageable);
            }

            return ResponseEntity.ok(BaseResponse.success(
                String.format("Found %d patients (page %d of %d)",
                    patients.getNumberOfElements(),
                    patients.getNumber() + 1,
                    patients.getTotalPages()),
                patients
            ));
        } catch (Exception e) {
            log.error("Failed to fetch patients for hospital {}: {}", hospitalId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch patients: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{patientId}")
    @Operation(
        summary = "Get Patient Details",
        description = "Retrieve complete patient information including medical history"
    )
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<PatientDto>> getPatientById(
            @PathVariable UUID patientId,
            Authentication authentication) {

        log.info("Fetching patient details: {}", patientId);

        try {
            Optional<PatientDto> patient = patientService.getPatientById(patientId);

            if (patient.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success("Patient found", patient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch patient: "+ e.getMessage())
            );
        }
    }

    /**
     * Search patients globally - Hospital Admin or Doctor (for cross-hospital visits)
     */
    @GetMapping("/search")
    @Operation(summary = "Search Patients Globally", description = "Search patients across all hospitals")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<BaseResponse<List<PatientDto>>> searchPatientsGlobally(
            @RequestParam String searchTerm) {

        try {
            List<PatientDto> patients = patientService.searchPatientsGlobally(searchTerm);
            return ResponseEntity.ok(BaseResponse.success("Patients found", patients));
        } catch (Exception e) {
            log.error("Failed to search patients globally: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to search patients: "+ e.getMessage())
            );
        }
    }

    /**
     * Update patient details - Hospital Admin, Doctor, or Receptionist
     */
    @PutMapping("/{patientId}")
    @Operation(summary = "Update Patient", description = "Update patient details")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<PatientDto>> updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreatePatientRequest request) {

        try {
            PatientDto updated = patientService.updatePatient(patientId, request);
            return ResponseEntity.ok(BaseResponse.success("Patient updated successfully", updated));
        } catch (Exception e) {
            log.error("Failed to update patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to update patient: "+ e.getMessage())
            );
        }
    }
}