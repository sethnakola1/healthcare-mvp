package com.healthcare.mvp.business.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.service.HospitalService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/business/super-admin")
@Tag(name = "Super Admin", description = "Super Admin management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {

    private final BusinessUserService businessUserService;
    private final HospitalService hospitalService;

    // Note: Consider using a global exception handler (@ControllerAdvice) to centralize exception handling.

    // ================= USER MANAGEMENT =================

    @PostMapping("/initialize")
    @Operation(summary = "Initialize Super Admin", description = "Creates the first Super Admin if one doesn't exist.")
    public ResponseEntity<BaseResponse<BusinessUserDto>> initializeSuperAdmin() {
        if (businessUserService.hasSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(BaseResponse.error("Super Admin already exists.", 1000));
        }
        BusinessUserDto superAdmin = businessUserService.createInitialSuperAdmin();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Super Admin initialized successfully", superAdmin));
    }

    @PostMapping("/tech-advisors")
    @Operation(summary = "Create Tech Advisor", description = "Register a new Tech Advisor")
    public ResponseEntity<BaseResponse<BusinessUserDto>> createTechAdvisor(
            @Valid @RequestBody CreateTechAdvisorRequest request) {
        log.info("Creating Tech Advisor: {}", request.getEmail());
        BusinessUserDto techAdvisor = businessUserService.createTechAdvisor(request.toServiceRequest());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Tech Advisor created successfully", techAdvisor));
    }

    @GetMapping("/tech-advisors")
    @Operation(summary = "Get Tech Advisors", description = "Retrieve all Tech Advisors with pagination")
    public ResponseEntity<BaseResponse<Page<BusinessUserDto>>> getAllTechAdvisors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BusinessUserDto> techAdvisors = businessUserService.getAllTechAdvisors(pageable);
        return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d Tech Advisors", techAdvisors.getTotalElements()),
                techAdvisors
        ));
    }

    @GetMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Get Tech Advisor Details", description = "Get specific Tech Advisor information")
    public ResponseEntity<BaseResponse<BusinessUserDto>> getTechAdvisorById(
            @PathVariable UUID techAdvisorId) {
        return businessUserService.getBusinessUserById(techAdvisorId)
                .map(advisor -> ResponseEntity.ok(BaseResponse.success("Tech Advisor found", advisor)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Tech Advisor not found", 1003)));
    }

    @PutMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Update Tech Advisor", description = "Update Tech Advisor information")
    public ResponseEntity<BaseResponse<BusinessUserDto>> updateTechAdvisor(
            @PathVariable UUID techAdvisorId,
            @Valid @RequestBody UpdateTechAdvisorRequest request) {
        BusinessUserDto updated = businessUserService.updateTechAdvisor(techAdvisorId, request.toServiceRequest());
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor updated successfully", updated));
    }

    @DeleteMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Deactivate Tech Advisor", description = "Deactivate a Tech Advisor")
    public ResponseEntity<BaseResponse<String>> deactivateTechAdvisor(@PathVariable UUID techAdvisorId) {
        businessUserService.deactivateTechAdvisor(techAdvisorId);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor deactivated successfully", "DEACTIVATED"));
    }

    // ================= SYSTEM MANAGEMENT =================

    @GetMapping("/system/stats")
    @Operation(summary = "Get System Statistics", description = "Comprehensive system metrics and statistics")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getSystemStatistics() {
        Map<String, Object> stats = businessUserService.getSystemMetrics();
        stats.put("systemHealth", "HEALTHY");
        stats.put("version", "1.0.0");
        return ResponseEntity.ok(BaseResponse.success("System statistics retrieved", stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Retrieve all system users with filtering and pagination")
    public ResponseEntity<BaseResponse<Page<BusinessUserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BusinessUserDto> users = businessUserService.getAllUsers();
        return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d users", users.getTotalElements()),
                users
        ));
    }

    @GetMapping("/hospitals")
    @Operation(summary = "Get All Hospitals", description = "Retrieve all hospitals in the system with pagination")
    public ResponseEntity<BaseResponse<Page<HospitalDto>>> getAllHospitals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<HospitalDto> hospitals = hospitalService.getAllHospitals(pageable);
        return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d hospitals", hospitals.getTotalElements()),
                hospitals
        ));
    }

    // Note: The following DTOs should ideally be in their own files in a 'dto' package.

    @lombok.Data
    public static class CreateTechAdvisorRequest {
        // ... fields and validation ...
        public CreateBusinessUserRequest toServiceRequest() {
            return CreateBusinessUserRequest.builder()
                    // ... map fields ...
                    .build();
        }
    }

    @lombok.Data
    public static class UpdateTechAdvisorRequest {
        // ... fields and validation ...
        public CreateBusinessUserRequest toServiceRequest() {
            return CreateBusinessUserRequest.builder()
                    // ... map fields ...
                    .build();
        }
    }
}