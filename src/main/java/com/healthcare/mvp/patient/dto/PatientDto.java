package com.healthcare.mvp.patient.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto {

    // Basic Patient Information
    private UUID patientId;
    private UUID hospitalId;
    private String hospitalName;
    private String globalPatientId;  // PAT0000001
    private String mrn;              // MRN000001

    // Personal Information
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;           // MALE, FEMALE, OTHER
    private String email;
    private String phoneNumber;
    private String address;
    private String bloodGroup;       // A+, B-, AB+, O-, etc.

    // Contact Information (JSON string)
    private String contactInfo;
    
    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    
    // Medical Information
    private String initialSymptoms;
    private String allergies;
    private String currentMedications;
    private String chronicConditions;
    
    // External System Integration
    private String fhirPatientId;

    // Security & Privacy
    private String ssnLast4;
    private Boolean isEncrypted;
    private UUID encryptionKeyId;

    // Status & Audit
    private Boolean isActive;
    private UUID createdBy;
    private UUID updatedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;

    // Computed Properties
    private Integer age;
    private String fullName;

    // Helper methods for computed properties
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }

    public Integer getAge() {
        if (dateOfBirth != null) {
            return LocalDate.now().getYear() - dateOfBirth.getYear();
        }
        return null;
    }

    // Display methods for UI
    public String getDisplayName() {
        return getFullName() + " (" + mrn + ")";
    }

    public String getContactDisplay() {
        StringBuilder contact = new StringBuilder();
        if (email != null && !email.trim().isEmpty()) {
            contact.append(email);
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (contact.length() > 0) contact.append(" | ");
            contact.append(phoneNumber);
        }
        return contact.toString();
    }

    // Getter/Setter for boolean compatibility
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
}