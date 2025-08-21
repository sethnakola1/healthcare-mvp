package com.healthcare.mvp.doctor.controller;

import com.healthcare.mvp.doctor.dto.CreateDoctorRequest;
import com.healthcare.mvp.doctor.dto.DoctorDto;
import com.healthcare.mvp.doctor.service.DoctorService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor Management", description = "Doctor registration and management")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    
    /**
     * Create doctor - Hospital Admin only
     */
    @PostMapping
    @Operation(summary = "Create Doctor", description = "Register a new doctor")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<DoctorDto>> createDoctor(
            @Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto doctor = doctorService.createDoctor(request);
        return ResponseEntity.ok(BaseResponse.success("Doctor created successfully", doctor));
    }
    
    /**
     * Get hospital doctors - Hospital Admin, Doctor, Nurse, Receptionist
     */
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get Hospital Doctors", description = "Get all doctors for a hospital")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<List<DoctorDto>>> getHospitalDoctors(
            @PathVariable UUID hospitalId) {
        List<DoctorDto> doctors = doctorService.getHospitalDoctors(hospitalId);
        return ResponseEntity.ok(BaseResponse.success("Doctors retrieved successfully", doctors));
    }
    
    /**
     * Get doctor by ID
     */
    @GetMapping("/{doctorId}")
    @Operation(summary = "Get Doctor Details", description = "Get specific doctor details")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<DoctorDto>> getDoctorById(@PathVariable UUID doctorId) {
        return doctorService.getDoctorById(doctorId)
                .map(doctor -> ResponseEntity.ok(BaseResponse.success("Doctor found", doctor)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get doctor by code
     */
    @GetMapping("/code/{doctorCode}")
    @Operation(summary = "Get Doctor by Code", description = "Get doctor by doctor code")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<DoctorDto>> getDoctorByCode(@PathVariable String doctorCode) {
        return doctorService.getDoctorByCode(doctorCode)
                .map(doctor -> ResponseEntity.ok(BaseResponse.success("Doctor found", doctor)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search doctors
     */
    @GetMapping("/search")
    @Operation(summary = "Search Doctors", description = "Search doctors by name or specialization")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<List<DoctorDto>>> searchDoctors(
            @RequestParam String searchTerm) {
        List<DoctorDto> doctors = doctorService.searchDoctors(searchTerm);
        return ResponseEntity.ok(BaseResponse.success("Doctors found", doctors));
    }
    
    /**
     * Get doctors by specialization
     */
    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get Doctors by Specialization", description = "Get doctors by specialization")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<List<DoctorDto>>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        List<DoctorDto> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(BaseResponse.success("Doctors retrieved successfully", doctors));
    }
    
    /**
     * Update doctor - Hospital Admin only
     */
    @PutMapping("/{doctorId}")
    @Operation(summary = "Update Doctor", description = "Update doctor details")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<DoctorDto>> updateDoctor(
            @PathVariable UUID doctorId,
            @Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto updated = doctorService.updateDoctor(doctorId, request);
        return ResponseEntity.ok(BaseResponse.success("Doctor updated successfully", updated));
    }
    
    /**
     * Deactivate doctor - Hospital Admin only
     */
    @DeleteMapping("/{doctorId}")
    @Operation(summary = "Deactivate Doctor", description = "Deactivate a doctor")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<String>> deactivateDoctor(@PathVariable UUID doctorId) {
        doctorService.deactivateDoctor(doctorId);
        return ResponseEntity.ok(BaseResponse.success("Doctor deactivated successfully", "Deactivated"));
    }
}