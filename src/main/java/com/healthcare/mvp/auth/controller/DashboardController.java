package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.appointment.service.AppointmentService;
import com.healthcare.mvp.auth.dto.DashboardDto;
import com.healthcare.mvp.billing.service.BillingService;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.prescription.service.PrescriptionService;
import com.healthcare.mvp.shared.constants.BusinessRole;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.exception.AuthorizationException;
import com.healthcare.mvp.shared.security.AuthenticationDetails;
import com.healthcare.mvp.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DashboardController {
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final PrescriptionService prescriptionService;
    private final BusinessUserService businessUserService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<DashboardDto>> getDashboard() {
        Authentication auth = SecurityUtils.getCurrentAuthentication();
        AuthenticationDetails details = (AuthenticationDetails) auth.getDetails();
        String role = details.getRoles().get(0); // Assume primary role
        DashboardDto dashboard = buildDashboard(role, details.getUserId(), details.getHospitalId());
        return ResponseEntity.ok(BaseResponse.success("Dashboard data retrieved", dashboard));
    }

    private DashboardDto buildDashboard(String role, String userId, String hospitalId) {
        DashboardDto dashboard = new DashboardDto();
        switch (BusinessRole.valueOf(role)) {
            case SUPER_ADMIN:
                dashboard.setSystemMetrics(businessUserService.getSystemMetrics());
                dashboard.setUsers(businessUserService.getAllUsers());
                break;
            case HOSPITAL_ADMIN:
                dashboard.setAppointments(appointmentService.getTodaysAppointments(UUID.fromString(hospitalId)));
                dashboard.setBills(billingService.getHospitalBills(UUID.fromString(hospitalId)));
                break;
            case DOCTOR:
                dashboard.setAppointments(appointmentService.getTodaysAppointmentsForDoctor(UUID.fromString(userId)));
                dashboard.setPrescriptions(prescriptionService.getDoctorPrescriptions(UUID.fromString(userId)));
                break;
            case PATIENT:
                dashboard.setAppointments(appointmentService.getTodaysAppointmentsForPatient(UUID.fromString(userId)));
                dashboard.setBills(billingService.getPatientBills(UUID.fromString(userId)));
                dashboard.setPrescriptions(prescriptionService.getPatientPrescriptions(UUID.fromString(userId)));
                break;
            // Add cases for other roles
            default:
                throw new AuthorizationException("Invalid role for dashboard access");
        }
        return dashboard;
    }
}