// CreateDoctorRequest.java
package com.healthcare.mvp.doctor.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateDoctorRequest {

    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format is required")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Medical license number is required")
    private String medicalLicenseNumber;

    private String qualification;

    @Min(value = 0, message = "Experience years must be positive")
    private Integer experienceYears;

    private String department;

    @DecimalMin(value = "0.0", message = "Consultation fee must be positive")
    private BigDecimal consultationFee;

    private List<String> availableDays;
    private String availableStartTime;
    private String availableEndTime;

    private LocalDate dateOfJoining;
    private String bio;
    private List<String> languagesSpoken;
    private Boolean isTelemedicineEnabled = false;

    // Constructors
    public CreateDoctorRequest() {}

    // Getters and Setters
    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public void setMedicalLicenseNumber(String medicalLicenseNumber) { this.medicalLicenseNumber = medicalLicenseNumber; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public String getAvailableStartTime() { return availableStartTime; }
    public void setAvailableStartTime(String availableStartTime) { this.availableStartTime = availableStartTime; }

    public String getAvailableEndTime() { return availableEndTime; }
    public void setAvailableEndTime(String availableEndTime) { this.availableEndTime = availableEndTime; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<String> getLanguagesSpoken() { return languagesSpoken; }
    public void setLanguagesSpoken(List<String> languagesSpoken) { this.languagesSpoken = languagesSpoken; }

    public Boolean getIsTelemedicineEnabled() { return isTelemedicineEnabled; }
    public void setIsTelemedicineEnabled(Boolean isTelemedicineEnabled) { this.isTelemedicineEnabled = isTelemedicineEnabled; }
}
