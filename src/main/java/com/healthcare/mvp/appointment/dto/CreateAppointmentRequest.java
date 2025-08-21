package com.healthcare.mvp.appointment.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateAppointmentRequest {
    
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be scheduled for a future date and time")
    private LocalDateTime appointmentDateTime;
    
    @Min(value = 15, message = "Appointment duration must be at least 15 minutes")
    @Max(value = 480, message = "Appointment duration cannot exceed 8 hours")
    private Integer durationMinutes = 30;
    
    @Pattern(regexp = "^(CONSULTATION|FOLLOW_UP|EMERGENCY|CHECKUP|PROCEDURE|SURGERY)$",
             message = "Appointment type must be one of: CONSULTATION, FOLLOW_UP, EMERGENCY, CHECKUP, PROCEDURE, SURGERY")
    private String appointmentType = "CONSULTATION";
    
    @Size(max = 500, message = "Chief complaint must not exceed 500 characters")
    private String chiefComplaint;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Boolean isVirtual = false;

    @Size(max = 255, message = "Meeting link must not exceed 255 characters")
    private String meetingLink;

    private Boolean isEmergency = false;

    private Boolean followUpRequired = false;

    private LocalDateTime followUpDate;

    // Default constructor
    public CreateAppointmentRequest() {
    }

    // Getters and Setters
    public UUID getHospitalId() {
        return hospitalId;
    }
    
    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
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
    
    // Validation methods
    @AssertTrue(message = "Meeting link is required for virtual appointments")
    public boolean isMeetingLinkValidForVirtualAppointment() {
        if (Boolean.TRUE.equals(isVirtual)) {
            return meetingLink != null && !meetingLink.trim().isEmpty();
        }
        return true;
    }
    
    @AssertTrue(message = "Follow-up date is required when follow-up is marked as required")
    public boolean isFollowUpDateValidWhenRequired() {
        if (Boolean.TRUE.equals(followUpRequired)) {
            return followUpDate != null && followUpDate.isAfter(appointmentDateTime);
        }
        return true;
    }
}
