package com.healthcare.mvp.doctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String doctorCode;

    @Column(nullable = false)
    private UUID hospitalId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String medicalLicenseNumber;

    @Column(nullable = false)
    private String qualification;

    @Column(nullable = false)
    private Integer experienceYears;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private BigDecimal consultationFee;

    private LocalDate dateOfJoining;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profilePictureUrl;

    @Builder.Default
    private Boolean isTelemedicineEnabled = false;

    @Builder.Default
    private Boolean isActive = true;

    private String availableDays;

    private String availableHours;

    private String languagesSpoken;

    private UUID createdBy;

    private UUID updatedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Additional helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
}