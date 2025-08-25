package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.appointment.service.AppointmentService;
import com.healthcare.mvp.auth.dto.DashboardMetrics;
import com.healthcare.mvp.auth.service.DashboardService;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.shared.constants.BusinessRole;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.AuthenticationDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Dashboard", description = "Role-based dashboard data and metrics")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final AppointmentService appointmentService;
    private final BusinessUserService businessUserService;

    /**
     * Get comprehensive dashboard data based on user role
     */
    @GetMapping
    @Operation(
        summary = "Get Dashboard Data",
        description = "Returns role-specific dashboard metrics and data",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<DashboardResponse>> getDashboard(Authentication auth) {
        try {
            AuthenticationDetails details = (AuthenticationDetails) auth.getDetails();
            String userId = details.getUserId();
            String userRole = details.getRoles().get(0); // Primary role
            String hospitalId = details.getHospitalId();

            log.info("Fetching dashboard for user: {} with role: {}", userId, userRole);

            DashboardResponse response = buildDashboardResponse(userRole, userId, hospitalId);

            return ResponseEntity.ok(BaseResponse.success("Dashboard data retrieved successfully", response));

        } catch (Exception e) {
            log.error("Error fetching dashboard data", e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch dashboard data: " + e.getMessage())
            );
        }
    }

    /**
     * Get Super Admin specific dashboard
     */
    @GetMapping("/super-admin")
    @Operation(summary = "Get Super Admin Dashboard", description = "Comprehensive system metrics for Super Admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BaseResponse<DashboardMetrics>> getSuperAdminDashboard() {
        try {
            DashboardMetrics metrics = dashboardService.getSuperAdminDashboard();

            // Add quick stats
            DashboardMetrics.QuickStats quickStats = buildSuperAdminQuickStats(metrics);
            metrics.setQuickStats(quickStats);

            return ResponseEntity.ok(BaseResponse.success("Super Admin dashboard retrieved", metrics));

        } catch (Exception e) {
            log.error("Error fetching Super Admin dashboard", e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch Super Admin dashboard: " + e.getMessage())
            );
        }
    }

    /**
     * Get system analytics for charts and graphs
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get System Analytics", description = "Chart data and analytics for dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String type) {

        try {
            Map<String, Object> analytics = buildAnalyticsData(period, type);
            return ResponseEntity.ok(BaseResponse.success("Analytics data retrieved", analytics));

        } catch (Exception e) {
            log.error("Error fetching analytics data", e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch analytics: " + e.getMessage(), type)
            );
        }
    }

    /**
     * Get recent activity logs
     */
    @GetMapping("/recent-activity")
    @Operation(summary = "Get Recent Activities", description = "Recent system activities and logs")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Map<String, Object> activities = buildRecentActivityData(limit);
            return ResponseEntity.ok(BaseResponse.success("Recent activities retrieved", activities));

        } catch (Exception e) {
            log.error("Error fetching recent activities", e);
            return ResponseEntity.badRequest().body(
                BaseResponse.error("Failed to fetch activities: " + e.getMessage())
            );
        }
    }

    // Helper Methods

    private DashboardResponse buildDashboardResponse(String userRole, String userId, String hospitalId) {
        DashboardResponse.DashboardResponseBuilder responseBuilder = DashboardResponse.builder()
                .userRole(userRole)
                .lastUpdated(LocalDateTime.now());

        DashboardMetrics metrics;

        switch (BusinessRole.valueOf(userRole)) {
            case SUPER_ADMIN:
                metrics = dashboardService.getSuperAdminDashboard();
                metrics.setQuickStats(buildSuperAdminQuickStats(metrics));
                responseBuilder.userName("Super Administrator");
                break;

            case TECH_ADVISOR:
                metrics = dashboardService.getTechAdvisorDashboard(UUID.fromString(userId));
                responseBuilder.userName("Tech Advisor");
                break;

            case HOSPITAL_ADMIN:
                metrics = dashboardService.getHospitalAdminDashboard(UUID.fromString(hospitalId));
                responseBuilder.userName("Hospital Administrator")
                              .hospitalName("Hospital Name"); // Fetch from service
                break;

            default:
                metrics = new DashboardMetrics();
                responseBuilder.userName("User");
        }

        // Add common data
        metrics.setRecentAppointments(appointmentService.getTodaysAppointments(
            hospitalId != null ? UUID.fromString(hospitalId) : null));

        // Build permissions map
        Map<String, Object> permissions = buildUserPermissions(userRole);

        return responseBuilder
                .metrics(metrics)
                .permissions(permissions)
                .build();
    }

    private DashboardMetrics.QuickStats buildSuperAdminQuickStats(DashboardMetrics metrics) {
        Map<String, Object> systemMetrics = metrics.getSystemMetrics();

        return DashboardMetrics.QuickStats.builder()
                .totalUsers(getLongValue(systemMetrics, "totalUsers"))
                .totalHospitals(getLongValue(systemMetrics, "totalHospitals"))
                .totalPatients(getLongValue(systemMetrics, "totalPatients"))
                .todayAppointments(0L) // Calculate from appointments
                .monthlyRevenue(java.math.BigDecimal.valueOf(150000))
                .totalCommission(java.math.BigDecimal.valueOf(30000))
                .growthPercentage(12.5)
                .activeSystemsCount(5)
                .build();
    }

    private Map<String, Object> buildAnalyticsData(String period, String type) {
        Map<String, Object> analytics = new HashMap<>();

        // User Registration Trends
        analytics.put("userRegistrations", buildChartData("User Registrations", "LINE"));

        // Hospital Onboarding
        analytics.put("hospitalOnboarding", buildChartData("Hospital Onboarding", "BAR"));

        // Revenue Trends
        analytics.put("revenueTrends", buildChartData("Revenue Trends", "LINE"));

        // Appointment Statistics
        analytics.put("appointmentStats", buildChartData("Appointments", "DOUGHNUT"));

        return analytics;
    }

    private DashboardMetrics.ChartData buildChartData(String label, String type) {
        return DashboardMetrics.ChartData.builder()
                .label(label)
                .type(type)
                .labels(java.util.List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
                .data(java.util.List.of(10, 20, 30, 25, 35, 40))
                .build();
    }

    private Map<String, Object> buildRecentActivityData(int limit) {
        Map<String, Object> activities = new HashMap<>();

        // Mock recent activities - replace with actual data
        java.util.List<DashboardMetrics.ActivityLog> logs = java.util.List.of(
            DashboardMetrics.ActivityLog.builder()
                .id("1")
                .userName("John Doe")
                .action("USER_CREATED")
                .description("New tech advisor registered")
                .timestamp(LocalDateTime.now().minusMinutes(10))
                .entityType("USER")
                .build(),
            DashboardMetrics.ActivityLog.builder()
                .id("2")
                .userName("Jane Smith")
                .action("HOSPITAL_CREATED")
                .description("New hospital onboarded")
                .timestamp(LocalDateTime.now().minusHours(1))
                .entityType("HOSPITAL")
                .build()
        );

        activities.put("logs", logs);
        activities.put("totalCount", logs.size());

        return activities;
    }

    private Map<String, Object> buildUserPermissions(String userRole) {
        Map<String, Object> permissions = new HashMap<>();

        switch (BusinessRole.valueOf(userRole)) {
            case SUPER_ADMIN:
                permissions.put("canCreateUsers", true);
                permissions.put("canManageHospitals", true);
                permissions.put("canViewAnalytics", true);
                permissions.put("canManageSystem", true);
                break;
            case TECH_ADVISOR:
                permissions.put("canCreateHospitals", true);
                permissions.put("canViewCommissions", true);
                break;
            case HOSPITAL_ADMIN:
                permissions.put("canManageStaff", true);
                permissions.put("canViewReports", true);
                permissions.put("canManageAppointments", true);
                break;
        }

        return permissions;
    }

    private long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardResponse {
        private String userRole;
        private String userName;
        private String hospitalName;
        private DashboardMetrics metrics;
        private java.util.List<DashboardMetrics.ChartData> charts;
        private java.util.List<DashboardMetrics.ActivityLog> recentActivities;
        private Map<String, Object> permissions;
        private LocalDateTime lastUpdated;
    }
}