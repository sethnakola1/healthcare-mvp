package com.healthcare.mvp.auth.dto;

import com.healthcare.mvp.appointment.dto.AppointmentDto;
import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetrics {

    // System-wide metrics (for Super Admin)
    private Map<String, Object> systemMetrics;
    private Map<String, Object> userAnalytics;
    private Map<String, Object> recentActivity;
    private Map<String, Object> revenueMetrics;

    // Personal metrics (for Tech Advisor)
    private Map<String, Object> personalMetrics;

    // Hospital specific metrics (for Hospital Admin)
    private Map<String, Object> hospitalMetrics;

    // Common data
    private List<AppointmentDto> recentAppointments;
    private List<BusinessUserDto> recentUsers;
    private List<HospitalDto> recentHospitals;
    private List<NotificationDto> notifications;

    // Quick stats for cards
    private QuickStats quickStats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuickStats {
        private long totalUsers;
        private long totalHospitals;
        private long totalPatients;
        private long todayAppointments;
        private BigDecimal monthlyRevenue;
        private BigDecimal totalCommission;
        private double growthPercentage;
        private int activeSystemsCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationDto {
        private String id;
        private String title;
        private String message;
        private String type; // INFO, WARNING, ERROR, SUCCESS
        private LocalDateTime timestamp;
        private boolean isRead;
        private String actionUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartData {
        private String label;
        private List<String> labels;
        private List<Number> data;
        private String type; // LINE, BAR, PIE, DOUGHNUT
        private Map<String, Object> options;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityLog {
        private String id;
        private String userId;
        private String userName;
        private String action;
        private String description;
        private LocalDateTime timestamp;
        private String entityType;
        private String entityId;
    }
}

// Enhanced Dashboard Response DTO
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class DashboardResponse {
    private String userRole;
    private String userName;
    private String hospitalName;
    private DashboardMetrics metrics;
    private List<DashboardMetrics.ChartData> charts;
    private List<DashboardMetrics.ActivityLog> recentActivities;
    private Map<String, Object> permissions;
    private LocalDateTime lastUpdated;
}