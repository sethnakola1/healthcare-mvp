package com.healthcare.mvp.appointment.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AppointmentDto {

    // Basic Appointment Information
    private UUID appointmentId;
    private UUID hospitalId;
    private String hospitalName;
    private UUID patientId;
    private String patientName;
    private String patientMrn;
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialization;
    
    // Appointment Details
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String status;  // SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    private String appointmentType;  // CONSULTATION, FOLLOW_UP, EMERGENCY, CHECKUP, PROCEDURE, SURGERY
    
    // Medical Information
    private String chiefComplaint;
    private String notes;
    private String cancellationReason;
    
    // Virtual Appointment Details
    private Boolean isVirtual;
    private String meetingLink;
    
    // Special Flags
    private Boolean isEmergency;
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;

    // Status & Audit
    private Boolean isActive;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;  // For compatibility with existing service
    private LocalDateTime updatedAt;  // For compatibility with existing service
    private OffsetDateTime createdDate;  // From entity
    private OffsetDateTime updatedDate;  // From entity
    private Long version;

    // Default constructor
    public AppointmentDto() {
    }
    
    // Getters and Setters
    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientMrn() {
        return patientMrn;
    }

    public void setPatientMrn(String patientMrn) {
        this.patientMrn = patientMrn;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Boolean getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(Boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public Boolean getIsEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(Boolean isEmergency) {
        this.isEmergency = isEmergency;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public OffsetDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(OffsetDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Helper methods
    public String getDisplayDateTime() {
        if (appointmentDateTime != null) {
            return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        }
        return "";
    }
    
    public String getStatusDisplay() {
        if (status != null) {
            return switch (status) {
                case "SCHEDULED" -> "Scheduled";
                case "CONFIRMED" -> "Confirmed";
                case "IN_PROGRESS" -> "In Progress";
                case "COMPLETED" -> "Completed";
                case "CANCELLED" -> "Cancelled";
                case "NO_SHOW" -> "No Show";
                default -> status;
            };
        }
        return "";
    }

    public String getPatientDisplay() {
        StringBuilder display = new StringBuilder();
        if (patientName != null && !patientName.trim().isEmpty()) {
            display.append(patientName);
        }
        if (patientMrn != null && !patientMrn.trim().isEmpty()) {
            if (display.length() > 0) display.append(" (");
            display.append(patientMrn);
            if (display.toString().contains("(")) display.append(")");
        }
        return display.toString();
    }
    
    public String getDoctorDisplay() {
        StringBuilder display = new StringBuilder();
        if (doctorName != null && !doctorName.trim().isEmpty()) {
            display.append(doctorName);
        }
        if (doctorSpecialization != null && !doctorSpecialization.trim().isEmpty()) {
            if (display.length() > 0) display.append(" - ");
            display.append(doctorSpecialization);
        }
        return display.toString();
    }
    
    public boolean isUpcoming() {
        return appointmentDateTime != null && appointmentDateTime.isAfter(LocalDateTime.now());
    }
    
    public boolean isPast() {
        return appointmentDateTime != null && appointmentDateTime.isBefore(LocalDateTime.now());
    }
    
    public boolean isToday() {
        if (appointmentDateTime != null) {
            return appointmentDateTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
        }
        return false;
    }
}
