package com.healthcare.mvp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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