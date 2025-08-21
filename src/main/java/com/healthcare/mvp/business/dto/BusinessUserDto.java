package com.healthcare.mvp.business.dto;

import com.healthcare.mvp.shared.constants.BusinessRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessUserDto {

    private UUID businessUserId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private BusinessRole businessRole;
    private String partnerCode;
    private BigDecimal commissionPercentage;
    private String territory;
    private Integer targetHospitalsMonthly;
    private Integer totalHospitalsBrought;
    private BigDecimal totalCommissionEarned;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Computed properties
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRoleDisplayName() {
        return businessRole != null ? businessRole.getDisplayName() : null;
    }
}