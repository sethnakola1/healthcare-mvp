package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.auth.dto.LoginRequest;
import com.healthcare.mvp.auth.dto.LoginResponse;
import com.healthcare.mvp.auth.dto.RefreshTokenRequest;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.audit.AuditLogger;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import com.healthcare.mvp.shared.util.JwtUtil;
import com.healthcare.mvp.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnhancedAuthenticationService {

    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;

    /**
     * Enhanced login with comprehensive audit logging and user context
     */
    public LoginResponse login(LoginRequest request) {
        log.info("=== ENHANCED LOGIN ATTEMPT START ===");
        log.info("Login attempt for email: {}", request.getEmail());

        // Audit the login attempt
        auditLogger.logAuthenticationEvent(null, request.getEmail(), "LOGIN_ATTEMPT", false);

        try {
            // Step 1: Find and validate user
            BusinessUser user = businessUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", request.getEmail());
                        auditLogger.logAuthenticationEvent(null, request.getEmail(), "LOGIN_FAILED_USER_NOT_FOUND", false);
                        return new AuthenticationException("Invalid email or password");
                    });

            log.debug("User found: ID={}, Role={}, Active={}",
                    user.getBusinessUserId(), user.getBusinessRole(), user.getIsActive());

            // Step 2: Validate account status
            validateAccountStatus(user, request.getEmail());

            // Step 3: Validate password
            validatePassword(request.getPassword(), user, request.getEmail());

            // Step 4: Update user login status
            updateLoginSuccess(user);

            // Step 5: Generate comprehensive JWT tokens
            LoginResponse response = generateLoginResponse(user);

            // Step 6: Audit successful login
            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                "LOGIN_SUCCESS",
                true
            );

            log.info("=== LOGIN SUCCESS ===");
            log.info("Successful login for user: {} with role: {}", request.getEmail(), user.getBusinessRole());

            return response;

        } catch (AuthenticationException e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Authentication failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());

            auditLogger.logAuthenticationEvent(null, request.getEmail(), "LOGIN_FAILED", false);
            throw e;
        } catch (Exception e) {
            log.error("=== LOGIN ERROR ===");
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);

            auditLogger.logAuthenticationEvent(null, request.getEmail(), "LOGIN_ERROR", false);
            throw new AuthenticationException("Login failed. Please try again.");
        }
    }

    /**
     * Enhanced token refresh with validation
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        try {
            if (!jwtUtil.validateToken(request.getRefreshToken())) {
                auditLogger.logSecurityEvent("INVALID_REFRESH_TOKEN", "Invalid refresh token used", "HIGH");
                throw new AuthenticationException("Invalid refresh token");
            }

            String userId = jwtUtil.getUserIdFromToken(request.getRefreshToken());
            BusinessUser user = businessUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.getIsActive()) {
                auditLogger.logSecurityEvent("INACTIVE_USER_TOKEN_REFRESH",
                    "Inactive user attempted token refresh: " + user.getEmail(), "HIGH");
                throw new AuthenticationException("Account is deactivated");
            }

            LoginResponse response = generateLoginResponse(user);
            response.setRefreshToken(request.getRefreshToken()); // Keep the same refresh token

            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                "TOKEN_REFRESH_SUCCESS",
                true
            );

            log.info("Token refreshed successfully for user: {}", user.getEmail());
            return response;

        } catch (AuthenticationException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            auditLogger.logSecurityEvent("TOKEN_REFRESH_ERROR", "Token refresh error: " + e.getMessage(), "MEDIUM");
            throw new AuthenticationException("Token refresh failed");
        }
    }

    /**
     * Enhanced logout with session cleanup
     */
    public void logout() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (currentUserId != null) {
            auditLogger.logAuthenticationEvent(currentUserId, currentUserEmail, "LOGOUT", true);
            log.info("User logged out: {} ({})", currentUserEmail, currentUserId);
        }
    }

    /**
     * Change password with security checks
     */
    public void changePassword(String currentPassword, String newPassword) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("User not authenticated");
        }

        BusinessUser user = businessUserRepository.findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditLogger.logSecurityEvent("INVALID_PASSWORD_CHANGE",
                "Invalid current password provided by: " + user.getEmail(), "HIGH");
            throw new AuthenticationException("Current password is incorrect");
        }

        // Encode and set new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        businessUserRepository.save(user);

        auditLogger.logAuthenticationEvent(
            user.getBusinessUserId().toString(),
            user.getEmail(),
            "PASSWORD_CHANGED",
            true
        );

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    /**
     * Get current user details with role information
     */
    public Map<String, Object> getCurrentUserDetails() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("User not authenticated");
        }

        BusinessUser user = businessUserRepository.findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AuthenticationException("User not found"));

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

        // Role-specific information
        Map<String, Object> rolePermissions = getRolePermissions(user.getBusinessRole());
        userDetails.put("permissions", rolePermissions);

        // Business-specific fields for Tech Advisor
        if (user.getBusinessRole().name().equals("TECH_ADVISOR")) {
            userDetails.put("commissionPercentage", user.getCommissionPercentage());
            userDetails.put("targetHospitalsMonthly", user.getTargetHospitalsMonthly());
            userDetails.put("totalHospitalsBrought", user.getTotalHospitalsBrought());
            userDetails.put("totalCommissionEarned", user.getTotalCommissionEarned());
        }

        return userDetails;
    }

    // ================= HELPER METHODS =================

    private void validateAccountStatus(BusinessUser user, String email) {
        if (!user.getIsActive()) {
            log.warn("Login attempt for inactive account: {}", email);
            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                email,
                "LOGIN_FAILED_INACTIVE_ACCOUNT",
                false
            );
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        if (user.getAccountLockedUntil() != null &&
            user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login attempt for locked account: {} (locked until: {})", email, user.getAccountLockedUntil());
            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                email,
                "LOGIN_FAILED_ACCOUNT_LOCKED",
                false
            );
            throw new AuthenticationException("Account is temporarily locked. Please try again later.");
        }
    }

    private void validatePassword(String providedPassword, BusinessUser user, String email) {
        String storedHash = user.getPasswordHash();

        // Check hash format
        if (storedHash == null || (!storedHash.startsWith("$2a$") && !storedHash.startsWith("$2b$"))) {
            log.error("Invalid password hash format for user: {}", email);
            auditLogger.logSecurityEvent("INVALID_PASSWORD_HASH",
                "Invalid password hash format for user: " + email, "CRITICAL");
            throw new AuthenticationException("Account password configuration error. Please contact support.");
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(providedPassword, storedHash);
        } catch (Exception e) {
            log.error("Error during password verification for user {}: {}", email, e.getMessage(), e);
            auditLogger.logSecurityEvent("PASSWORD_VERIFICATION_ERROR",
                "Password verification failed for user: " + email, "HIGH");
            throw new AuthenticationException("Password verification failed. Please contact support.");
        }

        if (!passwordMatches) {
            log.warn("Invalid password for user: {}", email);
            handleFailedLogin(user);
            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                email,
                "LOGIN_FAILED_INVALID_PASSWORD",
                false
            );
            throw new AuthenticationException("Invalid email or password");
        }
    }

    private void updateLoginSuccess(BusinessUser user) {
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        businessUserRepository.save(user);
    }

    private LoginResponse generateLoginResponse(BusinessUser user) {
        String role = user.getBusinessRole().name();
        String accessToken = jwtUtil.generateToken(
            user.getBusinessUserId().toString(),
            user.getEmail(),
            null, // hospitalId - will be null for business users
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getBusinessUserId().toString());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getBusinessUserId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(role)
                .loginTime(LocalDateTime.now())
                .build();
    }

    private void handleFailedLogin(BusinessUser user) {
        int attempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);

        // Lock account after 5 failed attempts for 30 minutes
        if (attempts >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("Account locked due to multiple failed login attempts: {}", user.getEmail());

            auditLogger.logSecurityEvent("ACCOUNT_LOCKED",
                "Account locked due to failed login attempts: " + user.getEmail(), "HIGH");
        }

        businessUserRepository.save(user);
    }

    private Map<String, Object> getRolePermissions(com.healthcare.mvp.shared.constants.BusinessRole role) {
        Map<String, Object> permissions = new HashMap<>();

        switch (role) {
            case SUPER_ADMIN:
                permissions.put("canCreateUsers", true);
                permissions.put("canManageSystem", true);
                permissions.put("canViewAnalytics", true);
                permissions.put("canManageHospitals", true);
                permissions.put("canViewFinancials", true);
                permissions.put("canAccessAuditLogs", true);
                break;
            case TECH_ADVISOR:
                permissions.put("canCreateHospitals", true);
                permissions.put("canViewCommissions", true);
                permissions.put("canManageOwnHospitals", true);
                permissions.put("canViewReports", true);
                break;
            case HOSPITAL_ADMIN:
                permissions.put("canManageStaff", true);
                permissions.put("canViewReports", true);
                permissions.put("canManageAppointments", true);
                permissions.put("canViewBilling", true);
                permissions.put("canManagePatients", true);
                break;
            default:
                permissions.put("canViewOwnData", true);
        }

        return permissions;
    }
}