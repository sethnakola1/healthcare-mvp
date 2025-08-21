package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.shared.constants.BusinessRole;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/users" )
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Admin - User Management", description = "Create and manage business users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementController {

    private final BusinessUserService businessUserService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create Business User", description = "Create a new Super Admin or Tech Advisor by a logged-in Super Admin.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BaseResponse<BusinessUserDto>> createBusinessUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Admin request to create user with email: {} and role: {}", request.getEmail(), request.getRole());

        try {
            // --- FIX: Correctly map the incoming request to the service layer request ---
            CreateBusinessUserRequest createRequest = CreateBusinessUserRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .phoneNumber(request.getPhoneNumber())
                    .territory(request.getTerritory())
                    .commissionPercentage(request.getRole() == BusinessRole.TECH_ADVISOR ? new BigDecimal("20.00") : BigDecimal.ZERO)
                    .targetHospitalsMonthly(request.getRole() == BusinessRole.TECH_ADVISOR ? 5 : 0)
                    .cognitoUserId("") // This can be set later
                    .build();

            BusinessUserDto newBusinessUser;
            if (request.getRole() == BusinessRole.SUPER_ADMIN) {
                newBusinessUser = businessUserService.createSuperAdmin(createRequest);
            } else if (request.getRole() == BusinessRole.TECH_ADVISOR) {
                newBusinessUser = businessUserService.createTechAdvisor(createRequest);
            } else {
                return ResponseEntity.badRequest().body(BaseResponse.error("INVALID_ROLE", "Invalid role specified for creation."));
            }

            return ResponseEntity.ok(BaseResponse.success("User created successfully", newBusinessUser));
        } catch (Exception e) {
            log.error("Admin failed to create user for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(BaseResponse.error("CREATION_FAILED", "Failed to create user: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset User Password", description = "Force-resets a user's password. For development/admin recovery.")
    public ResponseEntity<BaseResponse<String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.warn("Executing password reset for user: {}", request.getEmail());
        try {
            businessUserService.resetUserPassword(request.getEmail(), request.getNewPassword());

            // --- FIX: Use the correct BaseResponse factory method ---
            // This version takes a message and a data payload, matching the <String> return type.
            String successMessage = "Password for " + request.getEmail() + " has been reset successfully.";
            return ResponseEntity.ok(BaseResponse.success(successMessage, "SUCCESS"));

        } catch (Exception e) {
            log.error("Password reset failed for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(BaseResponse.error("RESET_FAILED", "Failed to reset password: " + e.getMessage()));
        }
    }

    // --- DTOs are defined as static inner classes for encapsulation ---

    @lombok.Data
    @PasswordMatches // Apply custom validator for password confirmation
    public static class UserRegistrationRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email;

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String password;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;

        private String phoneNumber;

        @NotBlank(message = "Territory is required")
        private String territory;

        @NotNull(message = "Role is required")
        private BusinessRole role;
    }

    @lombok.Data
    public static class PasswordResetRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        private String newPassword;
    }
}
