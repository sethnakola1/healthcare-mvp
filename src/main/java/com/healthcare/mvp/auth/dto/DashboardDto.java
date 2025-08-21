package com.healthcare.mvp.auth.dto;

import com.healthcare.mvp.appointment.dto.AppointmentDto;
import com.healthcare.mvp.billing.dto.BillingDto;
import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.notification.dto.NotificationDto;
import com.healthcare.mvp.prescription.dto.PrescriptionDto;
import com.healthcare.mvp.prescription.dto.UserFeedbackDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDto {
    private List<AppointmentDto> appointments;
    private List<BillingDto> bills;
    private List<PrescriptionDto> prescriptions;
    private List<BusinessUserDto> users;
    private Map<String, Object> systemMetrics; // For SUPER_ADMIN
    private List<UserFeedbackDto> feedback; // For HOSPITAL_ADMIN, DOCTOR
    private List<NotificationDto> notifications; // For all roles
}