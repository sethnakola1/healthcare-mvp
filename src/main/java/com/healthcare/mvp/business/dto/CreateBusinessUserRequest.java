package com.healthcare.mvp.business.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBusinessUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
             message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    private String password;

    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "Territory is required")
    @Size(max = 100, message = "Territory must not exceed 100 characters")
    private String territory;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.0", message = "Commission percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Commission percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal commissionPercentage;

    @NotNull(message = "Target hospitals monthly is required")
    @Min(value = 1, message = "Target hospitals monthly must be at least 1")
    @Max(value = 100, message = "Target hospitals monthly cannot exceed 100")
    private Integer targetHospitalsMonthly;


    private String cognitoUserId;
}