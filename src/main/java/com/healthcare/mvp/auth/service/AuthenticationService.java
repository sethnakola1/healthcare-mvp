package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.auth.dto.LoginRequest;
import com.healthcare.mvp.auth.dto.LoginResponse;
import com.healthcare.mvp.auth.dto.RefreshTokenRequest;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.audit.AuditLogger;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import com.healthcare.mvp.shared.security.AuthenticationDetails;
import com.healthcare.mvp.shared.util.JwtUtil;
import com.healthcare.mvp.shared.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import com.healthcare.mvp.shared.security.service.TokenBlocklistService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthenticationService {

    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;
    private final JwtBlocklistService jwtBlocklistService;
    private final TokenBlocklistService tokenBlocklistService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 30;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final long TOKEN_EXPIRES_IN = 86400L;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            BusinessUser user = findAndValidateUser(request.getEmail());
            validateAccountStatus(user);
            validatePassword(request.getPassword(), user);

            // Successful login
            updateSuccessfulLogin(user);
            String accessToken = generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user.getBusinessUserId(), user.getEmail());

            auditLogger.logAuthenticationEvent(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                "LOGIN",
                true
            );

            log.info("Successful login for user: {}", request.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(TOKEN_EXPIRES_IN)
                    .userId(user.getBusinessUserId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getBusinessRole().name())
                    .loginTime(LocalDateTime.now())
                    .build();

        } catch (AuthenticationException e) {
            // Attempt to find user to log their ID for better audit trails, without exposing existence.
            Optional<BusinessUser> userOpt = businessUserRepository.findByEmail(request.getEmail());
            String userIdForAudit = userOpt.map(u -> u.getBusinessUserId().toString()).orElse("N/A");

            auditLogger.logAuthenticationEvent(userIdForAudit, request.getEmail(), "LOGIN_FAILED", false);
            log.warn("Authentication failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            auditLogger.logAuthenticationEvent("N/A", request.getEmail(), "LOGIN_ERROR", false);
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new AuthenticationException("Login failed. Please try again.");
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            if (!jwtUtil.validateToken(request.getRefreshToken(), null)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String userId = jwtUtil.getUserIdFromToken(request.getRefreshToken());
            BusinessUser user = businessUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.getIsActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            String accessToken = generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getBusinessUserId(), user.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(TOKEN_EXPIRES_IN)
                    .userId(user.getBusinessUserId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getBusinessRole().name())
                    .loginTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed");
        }
    }

    private BusinessUser findAndValidateUser(String email) {
        return businessUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
    }

    private void validateAccountStatus(BusinessUser user) {
        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        if (user.getAccountLockedUntil() != null &&
            user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AuthenticationException(
                String.format("Account is locked until %s. Please try again later.",
                    user.getAccountLockedUntil())
            );
        }
    }

    private void validatePassword(String providedPassword, BusinessUser user) {
        if (user.getPasswordHash() == null ||
            (!user.getPasswordHash().startsWith("$2a$") && !user.getPasswordHash().startsWith("$2b$"))) {
            log.error("Invalid password hash format for user: {}", user.getEmail());
            throw new AuthenticationException("Account configuration error. Please contact support.");
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(providedPassword, user.getPasswordHash());
        } catch (Exception e) {
            log.error("Password verification failed for user {}: {}", user.getEmail(), e.getMessage());
            throw new AuthenticationException("Authentication failed. Please contact support.");
        }

        if (!passwordMatches) {
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid email or password");
        }
    }

    private void updateSuccessfulLogin(BusinessUser user) {
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        businessUserRepository.save(user);
    }

    private void handleFailedLogin(BusinessUser user) {
        int attempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            auditLogger.logSecurityEvent("ACCOUNT_LOCKED",
                "Account locked due to multiple failed login attempts: " + user.getEmail(),
                "HIGH");
        }

        businessUserRepository.save(user);
    }

    private String generateAccessToken(BusinessUser user) {
        String role = user.getBusinessRole().name();
        return jwtUtil.generateToken(
            user.getBusinessUserId().toString(),
            user.getEmail(),
            null, // hospitalId will be null for business users
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /**
     * Handles user logout. In a stateless JWT architecture, true server-side logout
     * requires a token denylist (e.g., using Redis). The token ID ('jti' claim) would be
     * stored in the denylist until it expires. The authentication filter would then
     * check this list on each request.
     *
     * For this implementation, we will log the logout event. The frontend is responsible
     * for deleting the tokens from storage.
     */
    public void logout(HttpServletRequest request) {
        try {
            String jwt = jwtUtil.getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                String jti = jwtUtil.getJtiFromToken(jwt);
                Date expiration = jwtUtil.getExpirationDateFromToken(jwt);
                jwtBlocklistService.blocklist(jti, expiration);
                log.info("Token {} blocklisted successfully.", jti);
            }

            BusinessUser user = getCurrentUser();
            log.info("User {} logging out.", user.getEmail());
            auditLogger.logAuthenticationEvent(user.getBusinessUserId().toString(), user.getEmail(), "LOGOUT", true);
            SecurityContextHolder.clearContext();
        } catch (AuthenticationException e) {
            log.warn("Logout attempt by unauthenticated user.");
        }
    }

    public void changePassword(
            @NotBlank(message = "Current password is required") String currentPassword,
            @NotBlank(message = "New password is required") @Size(min = 8, message = "New password must be at least 8 characters long") String newPassword) {

        BusinessUser user = getCurrentUser();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        businessUserRepository.save(user);

        auditLogger.logSecurityEvent("PASSWORD_CHANGED", "User " + user.getEmail() + " successfully changed their password.", "MEDIUM");
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void requestPasswordReset(@NotBlank(message = "Email is required") @Email(message = "Please provide a valid email address") String email) {
        // To prevent user enumeration, always behave as if the process was successful.
        businessUserRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_EXPIRY_MINUTES));
            businessUserRepository.save(user);

            // In a real application, you would send an email here.
            // emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("Password reset token generated for {}.", email);
            auditLogger.logSecurityEvent("PASSWORD_RESET_REQUESTED", "Password reset requested for " + email, "MEDIUM");
        });

        log.info("Password reset process initiated for email: {}", email);
    }

    @Transactional
    public void confirmPasswordReset(
            @NotBlank(message = "Token is required") String token,
            @NotBlank(message = "New password is required") @Size(min = 8, message = "New password must be at least 8 characters long") String newPassword) {

        BusinessUser user = businessUserRepository.findByPasswordResetTokenAndPasswordResetTokenExpiryAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired password reset token."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        businessUserRepository.save(user);

        auditLogger.logSecurityEvent("PASSWORD_RESET_COMPLETED", "Password reset completed for " + user.getEmail(), "HIGH");
        log.info("Password has been reset successfully for user: {}", user.getEmail());
    }

    private BusinessUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationException("No authenticated user found. Please log in.");
        }
        String email = authentication.getName();
        return businessUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Authenticated user not found in database. Please contact support."));
    }


    public void logout() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (currentUserId != null) {
            try {
                // Get the current JWT token from request context if available
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getDetails() instanceof AuthenticationDetails) {
                    // In a real implementation, you'd extract the actual JWT token from the request
                    // For now, we'll log the logout event
                    log.info("User logged out: {} ({})", currentUserEmail, currentUserId);

                    // Optional: Block all user tokens on logout for enhanced security
                    // Uncomment the next line if you want to invalidate all user tokens on logout
                    // tokenBlocklistService.blockAllUserTokens(UUID.fromString(currentUserId), "User logout");
                }
            } catch (Exception e) {
                log.error("Error during logout process for user {}: {}", currentUserId, e.getMessage());
            }
        }

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    /**
     * Logout with token blacklisting (when you have the actual token)
     */
    public void logoutWithToken(String accessToken, String refreshToken) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (currentUserId != null && currentUserEmail != null) {
            try {
                UUID userId = UUID.fromString(currentUserId);

                // Block the access token
                if (accessToken != null && !accessToken.isEmpty()) {
                    tokenBlocklistService.blockToken(accessToken, userId, currentUserEmail, "User logout");
                }

                // Block the refresh token
                if (refreshToken != null && !refreshToken.isEmpty()) {
                    tokenBlocklistService.blockRefreshToken(refreshToken, userId, currentUserEmail, "User logout");
                }

                log.info("Tokens blocked for user logout: {} ({})", currentUserEmail, currentUserId);

            } catch (Exception e) {
                log.error("Error blocking tokens during logout for user {}: {}", currentUserId, e.getMessage());
            }
        }

        // Clear security context
        SecurityContextHolder.clearContext();
    }
}
