package com.healthcare.mvp.business.entity;

import com.healthcare.mvp.shared.constants.BusinessRole;
import com.healthcare.mvp.shared.entity.LegacyBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class BusinessUser extends LegacyBaseEntity {

    public static final String BusinessRole = null;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "business_user_id")
    private UUID businessUserId;

    @Column(name = "cognito_user_id",
            unique = true)
    private String cognitoUserId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "business_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessRole businessRole;

    @Column(name = "partner_code", nullable = false, unique = true)
    private String partnerCode;

    @Column(name = "commission_percentage")
    @Builder.Default
    private BigDecimal commissionPercentage = new BigDecimal("20.00");

    @Column(name = "territory")
    private String territory;

    @Column(name = "target_hospitals_monthly")
    @Builder.Default
    private Integer targetHospitalsMonthly = 0;

    @Column(name = "total_hospitals_brought")
    @Builder.Default
    private Integer totalHospitalsBrought = 0;

    @Column(name = "total_commission_earned")
    @Builder.Default
    private BigDecimal totalCommissionEarned = BigDecimal.ZERO;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_attempts")
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    public BusinessUser(String firstName, String lastName, String email, String username,
                        BusinessRole businessRole, String cognitoUserId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.businessRole = businessRole;
        this.cognitoUserId = cognitoUserId;
        this.emailVerified = false;
        this.loginAttempts = 0;
        this.commissionPercentage = new BigDecimal("20.00");
        this.totalHospitalsBrought = 0;
        this.totalCommissionEarned = BigDecimal.ZERO;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate(); // Handles isActive initialization
        if (emailVerified == null) {
            emailVerified = false;
        }
        if (loginAttempts == null) {
            loginAttempts = 0;
        }
        if (commissionPercentage == null) {
            commissionPercentage = new BigDecimal("20.00");
        }
        if (totalHospitalsBrought == null) {
            totalHospitalsBrought = 0;
        }
        if (totalCommissionEarned == null) {
            totalCommissionEarned = BigDecimal.ZERO;
        }
    }
}