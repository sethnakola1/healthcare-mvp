package com.healthcare.mvp.doctor.entity;

import com.healthcare.mvp.shared.entity.LegacyBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor")
@Data
@EqualsAndHashCode(callSuper = true)
public class Doctor extends LegacyBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "doctor_code", unique = true)
    private String doctorCode;

    @Column(name = "cognito_user_id", unique = true)
    private String cognitoUserId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "department")
    private String department;

    @Column(name = "medical_license_number", nullable = false, unique = true)
    private String medicalLicenseNumber;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Column(name = "available_from")
    private LocalTime availableFrom;

    @Column(name = "available_to")
    private LocalTime availableTo;

    @Column(name = "available_days")
    private String availableDays;

    @Column(name = "available_hours")
    private String availableHours;

    @Column(name = "bio")
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "languages_spoken")
    private String languagesSpoken;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "is_telemedicine_enabled")
    private Boolean isTelemedicineEnabled = false;

    // Constructors
    public Doctor() {}

    public Doctor(String firstName, String lastName, String email, String specialization,
                 String medicalLicenseNumber, UUID hospitalId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.specialization = specialization;
        this.medicalLicenseNumber = medicalLicenseNumber;
        this.hospitalId = hospitalId;
        this.isTelemedicineEnabled = false;
    }

    // FIXED: Add missing getter/setter methods that were causing compilation errors

    /**
     * FIXED: Add missing setDoctorId method
     */
    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    /**
     * FIXED: Add missing getDoctorId method
     */
    public UUID getDoctorId() {
        return this.doctorId;
    }

    /**
     * FIXED: Add missing setSpecialization method
     */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    /**
     * FIXED: Add missing getSpecialization method
     */
    public String getSpecialization() {
        return this.specialization;
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        return "Dr. " + getFullName();
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (isTelemedicineEnabled == null) {
            isTelemedicineEnabled = false;
        }
        if (availableFrom == null) {
            availableFrom = LocalTime.of(9, 0);
        }
        if (availableTo == null) {
            availableTo = LocalTime.of(17, 0);
        }
    }
}