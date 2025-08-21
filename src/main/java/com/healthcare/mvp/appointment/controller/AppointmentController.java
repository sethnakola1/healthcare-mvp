package com.healthcare.mvp.appointment.controller;

import com.healthcare.mvp.appointment.dto.AppointmentDto;
import com.healthcare.mvp.appointment.dto.CreateAppointmentRequest;
import com.healthcare.mvp.appointment.service.AppointmentService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointment Management", description = "Appointment booking and management")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    /**
     * Book new appointment - Hospital Admin, Receptionist, or Patient
     */
    @PostMapping
    @Operation(summary = "Book Appointment", description = "Book a new appointment")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('RECEPTIONIST') or hasRole('PATIENT')")
    public ResponseEntity<BaseResponse<AppointmentDto>> bookAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentDto appointment = appointmentService.bookAppointment(request);
        return ResponseEntity.ok(BaseResponse.success("Appointment booked successfully", appointment));
    }
    
    /**
     * Get all appointments for hospital - Hospital Admin only
     */
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get Hospital Appointments", description = "Get all appointments for a hospital")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getHospitalAppointments(
            @PathVariable UUID hospitalId) {
        List<AppointmentDto> appointments = appointmentService.getHospitalAppointments(hospitalId);
        return ResponseEntity.ok(BaseResponse.success("Appointments retrieved successfully", appointments));
    }
    
    /**
     * Get doctor's appointments - Doctor or Hospital Admin
     */
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get Doctor Appointments", description = "Get appointments for specific doctor")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('HOSPITAL_ADMIN') or hasRole('NURSE')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getDoctorAppointments(
            @PathVariable UUID doctorId) {
        List<AppointmentDto> appointments = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(BaseResponse.success("Doctor appointments retrieved successfully", appointments));
    }
    
    /**
     * Get patient's appointments - Patient, Doctor, Nurse, or Hospital Admin
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get Patient Appointments", description = "Get appointments for specific patient")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getPatientAppointments(
            @PathVariable UUID patientId) {
        List<AppointmentDto> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(BaseResponse.success("Patient appointments retrieved successfully", appointments));
    }
    
    /**
     * Update appointment status - Doctor, Nurse, or Hospital Admin
     */
    @PutMapping("/{appointmentId}/status")
    @Operation(summary = "Update Appointment Status", description = "Update appointment status (confirm, cancel, complete)")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('HOSPITAL_ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<AppointmentDto>> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        AppointmentDto updated = appointmentService.updateAppointmentStatus(appointmentId, status, reason);
        return ResponseEntity.ok(BaseResponse.success("Appointment status updated successfully", updated));
    }
    
    /**
     * Cancel appointment - Patient, Doctor, or Hospital Admin
     */
    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Cancel Appointment", description = "Cancel an appointment")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('HOSPITAL_ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<String>> cancelAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam String reason) {
        appointmentService.cancelAppointment(appointmentId, reason);
        return ResponseEntity.ok(BaseResponse.success("Appointment cancelled successfully", "Cancelled"));
    }

    @GetMapping("/patient/{patientId}/today")
    @Operation(summary = "Get Today's Patient Appointments", description = "Get today's appointments for a specific patient")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getTodaysPatientAppointments(
            @PathVariable UUID patientId) {
        List<AppointmentDto> appointments = appointmentService.getTodaysAppointmentsForPatient(patientId);
        return ResponseEntity.ok(BaseResponse.success("Today's patient appointments retrieved successfully", appointments));
    }

    @GetMapping("/hospital/{hospitalId}/today")
    @Operation(summary = "Get Today's Hospital Appointments", description = "Get today's appointments for a hospital")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getTodaysHospitalAppointments(
            @PathVariable UUID hospitalId) {
        List<AppointmentDto> appointments = appointmentService.getTodaysAppointments(hospitalId);
        return ResponseEntity.ok(BaseResponse.success("Today's hospital appointments retrieved successfully", appointments));
    }

    @GetMapping("/doctor/{doctorId}/today")
    @Operation(summary = "Get Today's Doctor Appointments", description = "Get today's appointments for a specific doctor")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('HOSPITAL_ADMIN') or hasRole('NURSE')")
    public ResponseEntity<BaseResponse<List<AppointmentDto>>> getTodaysDoctorAppointments(
            @PathVariable UUID doctorId) {
        List<AppointmentDto> appointments = appointmentService.getTodaysAppointmentsForDoctor(doctorId);
        return ResponseEntity.ok(BaseResponse.success("Today's doctor appointments retrieved successfully", appointments));
    }


}