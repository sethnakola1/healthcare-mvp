package com.healthcare.mvp.business.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fixed DTO classes for SuperAdminController to resolve Lombok @Builder issues
 */
public class SuperAdminDtoClasses {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechAdvisorPerformanceDto {
        private UUID techAdvisorId;
        private String fullName;
        private String email;
        private String territory;
        private String partnerCode;
        
        // Performance metrics
        private Integer totalHospitalsBrought;
        private BigDecimal totalCommissionEarned;
        private BigDecimal commissionPercentage;
        private Integer targetHospitalsMonthly;
        
        // Status
        @Builder.Default
        private Boolean isActive = true;
        
        @Builder.Default
        private Boolean isTopPerformer = false;
        
        // Performance calculations
        private Double performanceScore;
        private String performanceGrade;
        private BigDecimal monthlyAverageCommission;
        
        // Audit
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdated;
        
        // Helper methods for computed properties
        public Double getPerformanceScore() {
            if (targetHospitalsMonthly != null && targetHospitalsMonthly > 0) {
                return (totalHospitalsBrought != null ? totalHospitalsBrought.doubleValue() : 0.0) 
                       / targetHospitalsMonthly.doubleValue() * 100.0;
            }
            return 0.0;
        }
        
        public String getPerformanceGrade() {
            Double score = getPerformanceScore();
            if (score >= 90) return "A+";
            if (score >= 80) return "A";
            if (score >= 70) return "B";
            if (score >= 60) return "C";
            return "D";
        }
        
        public BigDecimal getMonthlyAverageCommission() {
            if (totalCommissionEarned != null && totalHospitalsBrought != null && totalHospitalsBrought > 0) {
                return totalCommissionEarned.divide(new BigDecimal(totalHospitalsBrought), 2, BigDecimal.ROUND_HALF_UP);
            }
            return BigDecimal.ZERO;
        }
        
        public Boolean getIsTopPerformer() {
            return getPerformanceScore() >= 85.0;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemMetricsDto {
        // User metrics
        private Long totalUsers;
        private Long activeSuperAdmins;
        private Long activeTechAdvisors;
        
        // Hospital metrics  
        private Long totalHospitals;
        private Long activeHospitals;
        private Long newHospitalsThisMonth;
        
        // Business metrics
        private BigDecimal totalCommissionPaid;
        private BigDecimal monthlyRecurringRevenue;
        private Double averageCommissionRate;
        
        // System health
        @Builder.Default
        private String systemStatus = "HEALTHY";
        
        private LocalDateTime lastUpdated;
        private String version;
        
        // Performance indicators
        @Builder.Default
        private Double systemPerformance = 95.0;
        
        @Builder.Default  
        private Boolean maintenanceMode = false;
        
        public String getSystemStatus() {
            if (systemPerformance != null && systemPerformance >= 95.0) {
                return "EXCELLENT";
            } else if (systemPerformance >= 85.0) {
                return "GOOD";
            } else if (systemPerformance >= 70.0) {
                return "WARNING";
            } else {
                return "CRITICAL";
            }
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HospitalAnalyticsDto {
        private UUID hospitalId;
        private String hospitalName;
        private String hospitalCode;
        private String city;
        private String state;
        private String subscriptionPlan;
        
        // Analytics data
        private Long totalPatients;
        private Long totalAppointments;
        private Long totalDoctors;
        private BigDecimal monthlyRevenue;
        
        // Performance metrics
        @Builder.Default
        private Double utilizationRate = 0.0;
        
        @Builder.Default
        private Double satisfactionScore = 0.0;
        
        // Status
        @Builder.Default
        private Boolean isActive = true;
        
        private LocalDateTime contractStartDate;
        private LocalDateTime contractEndDate;
        private String broughtByTechAdvisor;
        
        // Computed properties
        public String getContractStatus() {
            if (contractEndDate != null && contractEndDate.isBefore(LocalDateTime.now())) {
                return "EXPIRED";
            }
            return "ACTIVE";
        }
        
        public Long getDaysUntilRenewal() {
            if (contractEndDate != null) {
                return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), contractEndDate);
            }
            return null;
        }
    }
}