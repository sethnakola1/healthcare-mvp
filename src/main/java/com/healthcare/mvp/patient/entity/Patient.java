package com.healthcare.mvp.patient.entity;

import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.shared.entity.BaseEntity;
import com.healthcare.mvp.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Patient extends BaseEntity {

    @Id
    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    private Hospital hospital;

    @Column(name = "global_patient_id", unique = true)
    private String globalPatientId; // PAT0000001

    @Column(name = "mrn", nullable = false)
    private String mrn; // Medical Record Number - Hospital-specific MRN

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "encrypted_firstname")
    private byte[] encryptedFirstname;

    @Column(name = "encrypted_lastname")
    private byte[] encryptedLastname;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "contact_info")
    private String contactInfo; // JSONB for phone, email, etc.

    @Column(name = "email")
    private String email;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "is_encrypted", nullable = false)
    @Builder.Default
    private boolean isEncrypted = false;

    @Column(name = "encryption_key_id")
    private UUID encryptionKeyId;

    // REMOVED: Duplicate isActive field - inherited from BaseEntity
    // @Column(name = "is_active", nullable = false)
    // @Builder.Default
    // private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Users createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private Users updatedByUser;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    // FIXED: Add proper getter/setter methods for boolean fields
    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    // Helper method for gender validation
    public void setGender(String gender) {
        if (gender != null) {
            try {
                Gender.valueOf(gender.toUpperCase());
                this.gender = gender.toUpperCase();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid gender value: " + gender +
                    ". Must be one of: MALE, FEMALE, OTHER");
            }
        } else {
            this.gender = null;
        }
    }

    // Add constraint validation for database
    @PrePersist
    @PreUpdate
    private void validateGender() {
        if (gender != null && !gender.matches("^(MALE|FEMALE|OTHER)$")) {
            throw new IllegalArgumentException("Gender must be MALE, FEMALE, or OTHER");
        }
    }
}