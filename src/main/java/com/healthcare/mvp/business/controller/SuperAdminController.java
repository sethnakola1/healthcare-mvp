package com.healthcare.mvp.business.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.hospital.service.HospitalService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business/super-admin")
@Tag(name = "Super Admin", description = "Super Admin management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SuperAdminController {

    @Autowired
    private BusinessUserService businessUserService;

    @Autowired
    private HospitalService hospitalService;

    /**
     * Initialize first Super Admin in database
     */
    @PostMapping("/initialize")
    @Operation(summary = "Initialize first Super Admin", description = "Creates initial Super Admin: Sethna Kola")
    public ResponseEntity<BaseResponse<BusinessUserDto>> initializeSuperAdmin() {
        try {
            BusinessUserDto superAdmin = businessUserService.createInitialSuperAdmin();
            return ResponseEntity.ok(BaseResponse.success("Super Admin initialized successfully", superAdmin));
        } catch (Exception e) {
            return ResponseEntity.ok(BaseResponse.success("Super Admin already exists or initialized", null));
        }
    }

    /**
     * Create Tech Advisor - FIXED: Using proper request DTO
     */
    @PostMapping("/tech-advisors")
    @Operation(summary = "Create Tech Advisor", description = "Create a new Tech Advisor by Super Admin")
    public ResponseEntity<BaseResponse<BusinessUserDto>> createTechAdvisor(@Valid @RequestBody CreateTechAdvisorRequest request) {
        // FIXED: Convert to CreateBusinessUserRequest which has all required methods
        CreateBusinessUserRequest createRequest = CreateBusinessUserRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail()) // Now available
                .username(request.getUsername())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .territory(request.getTerritory())
                .commissionPercentage(request.getCommissionPercentage())
                .targetHospitalsMonthly(request.getTargetHospitalsMonthly())
                .cognitoUserId("")
                .build();

        BusinessUserDto techAdvisor = businessUserService.createTechAdvisor(createRequest);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor created successfully", techAdvisor));
    }

    /**
     * Get all Tech Advisors - FIXED: Return Page instead of List
     */
    @GetMapping("/tech-advisors")
    @Operation(summary = "Get all Tech Advisors", description = "Retrieve list of all Tech Advisors with pagination")
    public ResponseEntity<BaseResponse<Page<BusinessUserDto>>> getAllTechAdvisors(Pageable pageable) {
        // FIXED: Use a method that returns Page<BusinessUserDto>
        Page<BusinessUserDto> techAdvisors = businessUserService.getAllTechAdvisorsPageable(pageable);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisors retrieved successfully", techAdvisors));
    }

    /**
     * Get Tech Advisor by ID
     */
    @GetMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Get Tech Advisor by ID", description = "Retrieve specific Tech Advisor details")
    public ResponseEntity<BaseResponse<BusinessUserDto>> getTechAdvisorById(@PathVariable UUID techAdvisorId) {
        return businessUserService.getBusinessUserById(techAdvisorId)
                .map(techAdvisor -> ResponseEntity.ok(BaseResponse.success("Tech Advisor found", techAdvisor)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update Tech Advisor
     */
    @PutMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Update Tech Advisor", description = "Update Tech Advisor details")
    public ResponseEntity<BaseResponse<BusinessUserDto>> updateTechAdvisor(
            @PathVariable UUID techAdvisorId,
            @Valid @RequestBody CreateTechAdvisorRequest request) {

        // Convert to CreateBusinessUserRequest
        CreateBusinessUserRequest updateRequest = CreateBusinessUserRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .territory(request.getTerritory())
                .commissionPercentage(request.getCommissionPercentage())
                .targetHospitalsMonthly(request.getTargetHospitalsMonthly())
                .cognitoUserId("")
                .build();

        BusinessUserDto updated = businessUserService.updateTechAdvisor(techAdvisorId, updateRequest);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor updated successfully", updated));
    }

    /**
     * Deactivate Tech Advisor
     */
    @DeleteMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Deactivate Tech Advisor", description = "Deactivate a Tech Advisor")
    public ResponseEntity<BaseResponse<String>> deactivateTechAdvisor(@PathVariable UUID techAdvisorId) {
        businessUserService.deactivateTechAdvisor(techAdvisorId);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor deactivated successfully", "Deactivated"));
    }

    /**
     * Get all hospitals - FIXED: Remove pageable parameter or add support for it
     */
    @GetMapping("/hospitals")
    @Operation(summary = "Get all hospitals", description = "Retrieve list of all hospitals")
    public ResponseEntity<BaseResponse<List<BusinessUserDto>>> getAllHospitals() {
        // FIXED: Call method without pageable parameter
        List<BusinessUserDto> hospitals = hospitalService.getAllHospitalsAsBusinessUsers();
        return ResponseEntity.ok(BaseResponse.success("Hospitals retrieved successfully", hospitals));
    }

    // ADDED: Missing CreateTechAdvisorRequest inner class with proper fields
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateTechAdvisorRequest {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email; // FIXED: Added missing email field

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String password;

        private String phoneNumber;

        @NotBlank(message = "Territory is required")
        private String territory;

        @NotNull(message = "Commission percentage is required")
        private java.math.BigDecimal commissionPercentage;

        @NotNull(message = "Target hospitals monthly is required")
        private Integer targetHospitalsMonthly;
    }
}