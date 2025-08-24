package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.auth.dto.*;
import com.healthcare.mvp.auth.service.AuthenticationService;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.SecurityUtils;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Authentication controller for user login/logout operations
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {"http://localhost:3000", "https://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = {"Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowCredentials = "true",
    maxAge = 3600
)
@Tag(name = "Authentication", description = "User authentication and authorization")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final BusinessUserRepository businessUserRepository;

    /**
     * Authenticate user with credentials
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        try {
            LoginResponse response = authenticationService.login(request);

            // Log successful login (without sensitive data)
            log.info("Login successful for user: {} with role: {}",
                    response.getEmail(), response.getRole());

            return ResponseEntity.ok(
                BaseResponse.success("Login successful", response)
            );

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("AUTHENTICATION_FAILED", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("LOGIN_ERROR", "An unexpected error occurred. Please try again."));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Generate new access token using refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");

        try {
            LoginResponse response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(
                BaseResponse.success("Token refreshed successfully", response)
            );

        } catch (AuthenticationException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("TOKEN_REFRESH_FAILED", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("REFRESH_ERROR", "Token refresh failed. Please log in again."));
        }
    }

    /**
     * Get current authenticated user details
     */
    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get current authenticated user details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getCurrentUser() {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("USER_NOT_AUTHENTICATED", "User not authenticated"));
        }

        try {
            BusinessUser user = businessUserRepository.findById(UUID.fromString(currentUserId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userDetails = buildUserDetailsResponse(user);

//            return ResponseEntity.ok(
//                BaseResponse.success("User details retrieved", userDetails)
//            );

            return ResponseEntity.ok(BaseResponse.success("User details retrieved", userDetails));

        } catch (Exception e) {
            log.error("Failed to get current user details for userId: {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("USER_DETAILS_ERROR", "Failed to get user details"));
        }
    }

    /**
     * Logout user (invalidate token on client side)
     */
    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logout user and invalidate session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<String>> logout(HttpServletRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Logout request received for user: {}", currentUserId);

        try {
            authenticationService.logout(request);
            return ResponseEntity.ok(
                BaseResponse.success("Logout successful", "User logged out successfully")
            );
        } catch (Exception e) {
            log.error("Error during logout for user: {}", currentUserId, e);
            return ResponseEntity.ok(
                BaseResponse.success("Logout completed", "Logged out")
            );
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change user password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Password change request received for user: {}", currentUserId);

        try {
            authenticationService.changePassword(request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(
                BaseResponse.success("Password changed successfully", "Your password has been updated")
            );

        } catch (AuthenticationException e) {
            log.warn("Password change failed for user: {} - Reason: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("PASSWORD_CHANGE_FAILED", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during password change for user: {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("PASSWORD_CHANGE_ERROR", "Failed to change password"));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Validate JWT token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Map<String, Object>>> validateToken() {
        String currentUserId = SecurityUtils.getCurrentUserId();

        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("valid", true);
        validationResult.put("userId", currentUserId);
        validationResult.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(
            BaseResponse.success("Token is valid", validationResult)
        );
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Auth Service Health Check")
    public ResponseEntity<BaseResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("service", "authentication");
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(
            BaseResponse.success("Auth service is healthy", healthInfo)
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Request Password Reset", description = "Initiate password reset by sending a reset link to the user's email")
    public ResponseEntity<BaseResponse<String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        try {
            authenticationService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(
                BaseResponse.success("Password reset link sent to email", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Failed to request password reset for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("RESET_FAILED", "Failed to request password reset: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm-reset")
    @Operation(summary = "Confirm Password Reset", description = "Complete password reset with token and new password")
    public ResponseEntity<BaseResponse<String>> confirmPasswordReset(@Valid @RequestBody ConfirmResetRequest request) {
        log.info("Confirming password reset for token");

        try {
            authenticationService.confirmPasswordReset(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(
                BaseResponse.success("Password reset successful", "SUCCESS")
            );

        } catch (Exception e) {
            log.error("Failed to reset password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("RESET_FAILED", "Failed to reset password: " + e.getMessage()));
        }
    }

    // Helper method to build user details response
    private Map<String, Object> buildUserDetailsResponse(BusinessUser user) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", user.getBusinessUserId().toString());
        userDetails.put("email", user.getEmail());
        userDetails.put("firstName", user.getFirstName());
        userDetails.put("lastName", user.getLastName());
        userDetails.put("fullName", user.getFullName());
        userDetails.put("username", user.getUsername());
        userDetails.put("role", user.getBusinessRole().name());
        userDetails.put("roleDisplayName", user.getBusinessRole().getDisplayName());
        userDetails.put("isActive", user.getIsActive());
        userDetails.put("emailVerified", user.getEmailVerified());
        userDetails.put("phoneNumber", user.getPhoneNumber());
        userDetails.put("territory", user.getTerritory());
        userDetails.put("partnerCode", user.getPartnerCode());
        userDetails.put("lastLogin", user.getLastLogin());
        userDetails.put("createdAt", user.getCreatedAt());

        // Add business-specific fields for Tech Advisors
        if (user.getBusinessRole().name().equals("TECH_ADVISOR")) {
            userDetails.put("commissionPercentage", user.getCommissionPercentage());
            userDetails.put("targetHospitalsMonthly", user.getTargetHospitalsMonthly());
            userDetails.put("totalHospitalsBrought", user.getTotalHospitalsBrought());
            userDetails.put("totalCommissionEarned", user.getTotalCommissionEarned());
        }

        return userDetails;
    }

    // DTOs
    @lombok.Data
    public static class PasswordResetRequest {
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Please provide a valid email address")
        private String email;
    }
}