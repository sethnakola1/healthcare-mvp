package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.appointment.repository.AppointmentRepository;
import com.healthcare.mvp.auth.dto.DashboardMetrics;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import com.healthcare.mvp.patient.repository.PatientRepository;
import com.healthcare.mvp.shared.constants.BusinessRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final BusinessUserRepository businessUserRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Get comprehensive dashboard metrics for Super Admin
     */
    public DashboardMetrics getSuperAdminDashboard() {
        log.info("Fetching Super Admin dashboard metrics");

        DashboardMetrics metrics = new DashboardMetrics();

        // System Overview
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("totalUsers", businessUserRepository.count());
        systemMetrics.put("activeUsers", businessUserRepository.countActiveUsersByRole(BusinessRole.SUPER_ADMIN) +
                                        businessUserRepository.countActiveUsersByRole(BusinessRole.TECH_ADVISOR));
        systemMetrics.put("totalHospitals", hospitalRepository.count());
        systemMetrics.put("activeHospitals", hospitalRepository.countByIsActive(true));
        systemMetrics.put("totalPatients", patientRepository.countAllActivePatients());
        systemMetrics.put("totalAppointments", appointmentRepository.count());

        // User Analytics
        Map<String, Object> userAnalytics = new HashMap<>();
        userAnalytics.put("superAdmins", businessUserRepository.countActiveUsersByRole(BusinessRole.SUPER_ADMIN));
        userAnalytics.put("techAdvisors", businessUserRepository.countActiveUsersByRole(BusinessRole.TECH_ADVISOR));

        // Recent Activity (last 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        Map<String, Object> recentActivity = new HashMap<>();
        // Add queries for recent registrations, logins, etc.

        // Revenue Metrics (placeholder - implement based on your billing system)
        Map<String, Object> revenueMetrics = new HashMap<>();
        revenueMetrics.put("monthlyRevenue", BigDecimal.valueOf(150000));
        revenueMetrics.put("totalRevenue", BigDecimal.valueOf(2500000));
        revenueMetrics.put("averageRevenuePerHospital", BigDecimal.valueOf(12500));

        metrics.setSystemMetrics(systemMetrics);
        metrics.setUserAnalytics(userAnalytics);
        metrics.setRecentActivity(recentActivity);
        metrics.setRevenueMetrics(revenueMetrics);

        return metrics;
    }

    /**
     * Get dashboard for Tech Advisor
     */
    public DashboardMetrics getTechAdvisorDashboard(UUID techAdvisorId) {
        log.info("Fetching Tech Advisor dashboard for: {}", techAdvisorId);

        DashboardMetrics metrics = new DashboardMetrics();

        // Get hospitals brought by this tech advisor
        var hospitals = hospitalRepository.findByBroughtByBusinessUser(techAdvisorId);

        Map<String, Object> personalMetrics = new HashMap<>();
        personalMetrics.put("hospitalsReferred", hospitals.size());
        personalMetrics.put("activeHospitals", hospitals.stream().mapToInt(h -> h.getIsActive() ? 1 : 0).sum());

        // Calculate commission (placeholder)
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (var hospital : hospitals) {
            if (hospital.getMonthlyRevenue() != null && hospital.getCommissionRate() != null) {
                totalCommission = totalCommission.add(
                    BigDecimal.valueOf(hospital.getMonthlyRevenue())
                    .multiply(BigDecimal.valueOf(hospital.getCommissionRate()))
                    .divide(BigDecimal.valueOf(100))
                );
            }
        }
        personalMetrics.put("monthlyCommission", totalCommission);

        metrics.setPersonalMetrics(personalMetrics);

        return metrics;
    }

    /**
     * Get dashboard for Hospital Admin
     */
    public DashboardMetrics getHospitalAdminDashboard(UUID hospitalId) {
        log.info("Fetching Hospital Admin dashboard for hospital: {}", hospitalId);

        DashboardMetrics metrics = new DashboardMetrics();

        // Hospital specific metrics
        Map<String, Object> hospitalMetrics = new HashMap<>();
        hospitalMetrics.put("totalPatients", patientRepository.countActivePatientsByHospital(hospitalId));
        hospitalMetrics.put("totalDoctors", 0); // Add when DoctorRepository is available

        // Today's appointments
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        var todayAppointments = appointmentRepository.findTodaysAppointmentsByHospital(hospitalId, startOfDay, endOfDay);
        hospitalMetrics.put("todayAppointments", todayAppointments.size());
        hospitalMetrics.put("pendingAppointments", todayAppointments.stream()
            .mapToInt(a -> "SCHEDULED".equals(a.getStatus().name()) ? 1 : 0).sum());

        metrics.setHospitalMetrics(hospitalMetrics);

        return metrics;
    }
}