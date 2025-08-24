package com.healthcare.mvp.business.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.service.HospitalService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // ================= USER MANAGEMENT =================

    /**
     * Initialize first Super Admin (for system setup only)
     */
    @PostMapping("/initialize")
    @Operation(summary = "Initialize Super Admin", description = "Creates the first Super Admin (setup only)")
    public ResponseEntity<BaseResponse<BusinessUserDto>> initializeSuperAdmin() {
        try {
            BusinessUserDto superAdmin = businessUserService.createInitialSuperAdmin();
            return ResponseEntity.ok(BaseResponse.success("Super Admin initialized successfully", superAdmin));
        } catch (Exception e) {
            log.warn("Super Admin initialization failed: {}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.success("Super Admin already exists", null));
        }
    }

    /**
     * Create new Tech Advisor
     */
    @PostMapping("/tech-advisors")
    @Operation(summary = "Create Tech Advisor", description = "Register a new Tech Advisor")
    public ResponseEntity<BaseResponse<BusinessUserDto>> createTechAdvisor(
            @Valid @RequestBody CreateTechAdvisorRequest request) {

        log.info("Creating Tech Advisor: {}", request.getEmail());

        try {
            // Convert to service request
            CreateBusinessUserRequest serviceRequest = CreateBusinessUserRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .phoneNumber(request.getPhoneNumber())
                    .territory(request.getTerritory())
                    .commissionPercentage(request.getCommissionPercentage())
                    .targetHospitalsMonthly(request.getTargetHospitalsMonthly())
                    .build();

            BusinessUserDto techAdvisor = businessUserService.createTechAdvisor(serviceRequest);

            return ResponseEntity.ok(BaseResponse.success("Tech Advisor created successfully", techAdvisor));

        } catch (Exception e) {
            log.error("Failed to create Tech Advisor: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to create Tech Advisor: " + e.getMessage())
            );
        }
    }

    /**
     * Get all Tech Advisors with pagination
     */
    @GetMapping("/tech-advisors")
    @Operation(summary = "Get Tech Advisors", description = "Retrieve all Tech Advisors with pagination")
    public ResponseEntity<BaseResponse<List<BusinessUserDto>>> getAllTechAdvisors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            List<BusinessUserDto> techAdvisors = businessUserService.getAllTechAdvisors();

            return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d Tech Advisors", techAdvisors.size()),
                techAdvisors
            ));

        } catch (Exception e) {
            log.error("Failed to fetch Tech Advisors: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch Tech Advisors: " + e.getMessage())
            );
        }
    }

    /**
     * Get Tech Advisor by ID
     */
    @GetMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Get Tech Advisor Details", description = "Get specific Tech Advisor information")
    public ResponseEntity<BaseResponse<BusinessUserDto>> getTechAdvisorById(
            @PathVariable UUID techAdvisorId) {

        try {
            return businessUserService.getBusinessUserById(techAdvisorId)
                    .map(advisor -> ResponseEntity.ok(BaseResponse.success("Tech Advisor found", advisor)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Failed to fetch Tech Advisor {}: {}", techAdvisorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch Tech Advisor: " + e.getMessage())
            );
        }
    }

    /**
     * Update Tech Advisor
     */
    @PutMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Update Tech Advisor", description = "Update Tech Advisor information")
    public ResponseEntity<BaseResponse<BusinessUserDto>> updateTechAdvisor(
            @PathVariable UUID techAdvisorId,
            @Valid @RequestBody UpdateTechAdvisorRequest request) {

        try {
            CreateBusinessUserRequest serviceRequest = CreateBusinessUserRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .territory(request.getTerritory())
                    .commissionPercentage(request.getCommissionPercentage())
                    .targetHospitalsMonthly(request.getTargetHospitalsMonthly())
                    .build();

            BusinessUserDto updated = businessUserService.updateTechAdvisor(techAdvisorId, serviceRequest);

            return ResponseEntity.ok(BaseResponse.success("Tech Advisor updated successfully", updated));

        } catch (Exception e) {
            log.error("Failed to update Tech Advisor {}: {}", techAdvisorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to update Tech Advisor: " + e.getMessage())
            );
        }
    }

    /**
     * Deactivate Tech Advisor
     */
    @DeleteMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Deactivate Tech Advisor", description = "Deactivate a Tech Advisor")
    public ResponseEntity<BaseResponse<String>> deactivateTechAdvisor(@PathVariable UUID techAdvisorId) {
        try {
            businessUserService.deactivateTechAdvisor(techAdvisorId);
            return ResponseEntity.ok(BaseResponse.success("Tech Advisor deactivated successfully", "DEACTIVATED"));

        } catch (Exception e) {
            log.error("Failed to deactivate Tech Advisor {}: {}", techAdvisorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to deactivate Tech Advisor: " + e.getMessage())
            );
        }
    }

    // ================= SYSTEM MANAGEMENT =================

    /**
     * Get comprehensive system statistics
     */
    @GetMapping("/system/stats")
    @Operation(summary = "Get System Statistics", description = "Comprehensive system metrics and statistics")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getSystemStatistics() {
        try {
            Map<String, Object> stats = businessUserService.getSystemMetrics();

            // Add additional system stats
            stats.put("systemUptime", getSystemUptime());
            stats.put("lastUpdated", LocalDateTime.now());
            stats.put("systemHealth", "HEALTHY");
            stats.put("version", "1.0.0");

            return ResponseEntity.ok(BaseResponse.success("System statistics retrieved", stats));

        } catch (Exception e) {
            log.error("Failed to fetch system statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch system statistics: " + e.getMessage())
            );
        }
    }

    /**
     * Get all users in the system
     */
    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Retrieve all system users")
    public ResponseEntity<BaseResponse<List<BusinessUserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        try {
            List<BusinessUserDto> users = businessUserService.getAllUsers();

            // Filter by role if specified
            if (role != null && !role.trim().isEmpty()) {
                users = users.stream()
                    .filter(user -> role.equalsIgnoreCase(user.getBusinessRole().name()))
                    .toList();
            }

            // Filter by status if specified
            if (status != null && !status.trim().isEmpty()) {
                boolean isActive = "active".equalsIgnoreCase(status);
                users = users.stream()
                    .filter(user -> user.getIsActive() == isActive)
                    .toList();
            }

            return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d users", users.size()),
                users
            ));

        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch users: " + e.getMessage())
            );
        }
    }

    /**
     * Get all hospitals in the system
     */
    @GetMapping("/hospitals")
    @Operation(summary = "Get All Hospitals", description = "Retrieve all hospitals in the system")
    public ResponseEntity<BaseResponse<List<HospitalDto>>> getAllHospitals() {
        try {
            List<HospitalDto> hospitals = hospitalService.getAllHospitals();

            return ResponseEntity.ok(BaseResponse.success(
                String.format("Retrieved %d hospitals", hospitals.size()),
                hospitals
            ));

        } catch (Exception e) {
            log.error("Failed to fetch hospitals: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch hospitals: " + e.getMessage())
            );
        }
    }

    /**
     * Get performance analytics
     */
    @GetMapping("/analytics/performance")
    @Operation(summary = "Get Performance Analytics", description = "System performance and usage analytics")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getPerformanceAnalytics(
            @RequestParam(defaultValue = "30") int days) {

        try {
            Map<String, Object> analytics = new HashMap<>();

            // Mock analytics data - replace with actual metrics
            analytics.put("userGrowth", generateGrowthData(days));
            analytics.put("hospitalOnboarding", generateOnboardingData(days));
            analytics.put("systemUsage", generateUsageData(days));
            analytics.put("revenueMetrics", generateRevenueData(days));

            return ResponseEntity.ok(BaseResponse.success("Analytics retrieved", analytics));

        } catch (Exception e) {
            log.error("Failed to fetch analytics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch analytics: " + e.getMessage())
            );
        }
    }

    // ================= UTILITY METHODS =================

    private String getSystemUptime() {
        long uptime = System.currentTimeMillis() / 1000 / 60; // minutes
        return uptime + " minutes";
    }

    private Map<String, Object> generateGrowthData(int days) {
        Map<String, Object> growth = new HashMap<>();
        growth.put("totalUsers", 150);
        growth.put("newUsers", 25);
        growth.put("growthRate", 12.5);
        return growth;
    }

    private Map<String, Object> generateOnboardingData(int days) {
        Map<String, Object> onboarding = new HashMap<>();
        onboarding.put("newHospitals", 5);
        onboarding.put("pendingApprovals", 2);
        onboarding.put("completedSetups", 3);
        return onboarding;
    }

    private Map<String, Object> generateUsageData(int days) {
        Map<String, Object> usage = new HashMap<>();
        usage.put("activeUsers", 120);
        usage.put("dailyLogins", 350);
        usage.put("avgSessionDuration", "25 minutes");
        return usage;
    }

    private Map<String, Object> generateRevenueData(int days) {
        Map<String, Object> revenue = new HashMap<>();
        revenue.put("totalRevenue", BigDecimal.valueOf(250000));
        revenue.put("monthlyRevenue", BigDecimal.valueOf(45000));
        revenue.put("projectedRevenue", BigDecimal.valueOf(300000));
        return revenue;
    }

    // ================= REQUEST/RESPONSE DTOs =================

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CreateTechAdvisorRequest {
        @jakarta.validation.constraints.NotBlank(message = "First name is required")
        private String firstName;

        @jakarta.validation.constraints.NotBlank(message = "Last name is required")
        private String lastName;

        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Valid email is required")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        private String username;

        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        private String phoneNumber;

        @jakarta.validation.constraints.NotBlank(message = "Territory is required")
        private String territory;

        @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Commission percentage must be positive")
        @jakarta.validation.constraints.DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100%")
        private BigDecimal commissionPercentage = new BigDecimal("20.00");

        @jakarta.validation.constraints.Min(value = 1, message = "Target must be at least 1")
        private Integer targetHospitalsMonthly = 5;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UpdateTechAdvisorRequest {
        @jakarta.validation.constraints.NotBlank(message = "First name is required")
        private String firstName;

        @jakarta.validation.constraints.NotBlank(message = "Last name is required")
        private String lastName;

        private String phoneNumber;

        @jakarta.validation.constraints.NotBlank(message = "Territory is required")
        private String territory;

        @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Commission percentage must be positive")
        @jakarta.validation.constraints.DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100%")
        private BigDecimal commissionPercentage;

        @jakarta.validation.constraints.Min(value = 1, message = "Target must be at least 1")
        private Integer targetHospitalsMonthly;
    }
}