package com.healthcare.mvp.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePatientRequest {
    
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (E.164 format)")
    private String phoneNumber;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be valid (e.g., A+, B-, AB+, O-)")
    private String bloodGroup;

    @Size(max = 1000, message = "Emergency contact name must not exceed 1000 characters")
    private String emergencyContactName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Emergency contact phone must be valid")
    private String emergencyContactPhone;

    @Size(max = 100, message = "Emergency contact relationship must not exceed 100 characters")
    private String emergencyContactRelationship;

    @Size(max = 2000, message = "Initial symptoms must not exceed 2000 characters")
    private String initialSymptoms;

    @Size(max = 1000, message = "Allergies must not exceed 1000 characters")
    private String allergies;

    @Size(max = 1000, message = "Current medications must not exceed 1000 characters")
    private String currentMedications;

    @Size(max = 1000, message = "Chronic conditions must not exceed 1000 characters")
    private String chronicConditions;

    @Size(max = 100, message = "FHIR Patient ID must not exceed 100 characters")
    private String fhirPatientId;

    @Pattern(regexp = "^\\d{4}$", message = "SSN last 4 digits must be exactly 4 digits")
    private String ssnLast4;
}