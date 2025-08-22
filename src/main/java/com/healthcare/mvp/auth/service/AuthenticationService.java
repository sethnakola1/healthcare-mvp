package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.auth.dto.LoginRequest;
import com.healthcare.mvp.auth.dto.LoginResponse;
import com.healthcare.mvp.auth.dto.RefreshTokenRequest;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import com.healthcare.mvp.shared.util.JwtUtil;
import com.healthcare.mvp.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    public LoginResponse login(LoginRequest request) {
        log.info("=== LOGIN ATTEMPT START ===");
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Step 1: Find and validate user
            BusinessUser user = businessUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", request.getEmail());
                        return new AuthenticationException("Invalid email or password");
                    });

            log.debug("User found: ID={}, Name={} {}, Role={}",
                    user.getBusinessUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getBusinessRole());

            // Step 2: Validate account status
            validateAccountStatus(user, request.getEmail());

            // Step 3: Validate password
            validatePassword(request.getPassword(), user);

            // Step 4: Update login success data
            updateLoginSuccess(user);

            // Step 5: Generate tokens
            LoginResponse response = generateLoginResponse(user);

            log.info("=== LOGIN SUCCESS ===");
            log.info("Successful login for user: {} with role: {}", request.getEmail(), user.getBusinessRole());

            return response;

        } catch (AuthenticationException e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Authentication failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== LOGIN ERROR ===");
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new AuthenticationException("Login failed. Please try again.");
        }
    }

    private void validateAccountStatus(BusinessUser user, String email) {
        if (!user.getIsActive()) {
            log.warn("Login attempt for inactive account: {}", email);
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        if (user.getAccountLockedUntil() != null &&
            user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login attempt for locked account: {} (locked until: {})",
                    email, user.getAccountLockedUntil());
            throw new AuthenticationException("Account is temporarily locked. Please try again later.");
        }

        if (!user.getEmailVerified()) {
            log.warn("Login attempt for unverified email: {}", email);
            throw new AuthenticationException("Please verify your email address before logging in.");
        }
    }

    private void validatePassword(String providedPassword, BusinessUser user) {
        String storedHash = user.getPasswordHash();

        if (storedHash == null || (!storedHash.startsWith("$2a$") && !storedHash.startsWith("$2b$"))) {
            log.error("Invalid password hash format for user: {}", user.getEmail());
            throw new AuthenticationException("Account password configuration error. Please contact support.");
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(providedPassword, storedHash);
            log.debug("Password match result: {}", passwordMatches);
        } catch (Exception e) {
            log.error("Error during password verification for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new AuthenticationException("Password verification failed. Please contact support.");
        }

        if (!passwordMatches) {
            log.warn("Invalid password for user: {}", user.getEmail());
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid email or password");
        }
    }

    private void updateLoginSuccess(BusinessUser user) {
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        businessUserRepository.save(user);
    }

    private LoginResponse generateLoginResponse(BusinessUser user) {
        String role = user.getBusinessRole().name();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // Generate access token
        String accessToken = jwtUtil.generateToken(
            user.getBusinessUserId().toString(),
            user.getEmail(),
            null, // hospitalId - will be null for business users
            authorities
        );

        // Generate refresh token
        String refreshToken = jwtUtil.generateRefreshToken(user.getBusinessUserId().toString());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTimeInSeconds())
                .userId(user.getBusinessUserId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(role)
                .loginTime(LocalDateTime.now())
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        try {
            String refreshToken = request.getRefreshToken();

            if (!jwtUtil.validateToken(refreshToken)) {
                throw new AuthenticationException("Invalid or expired refresh token");
            }

            String userId = jwtUtil.getUserIdFromToken(refreshToken);
            BusinessUser user = businessUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.getIsActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            // Generate new access token
            String role = user.getBusinessRole().name();
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            String accessToken = jwtUtil.generateToken(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                null,
                authorities
            );

            log.info("Token refreshed successfully for user: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken) // Keep the same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTimeInSeconds())
                    .userId(user.getBusinessUserId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(role)
                    .loginTime(LocalDateTime.now())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            throw new AuthenticationException("Token refresh failed");
        }
    }

    public void logout() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            log.info("User logged out: {}", currentUserId);
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("User not authenticated");
        }

        BusinessUser user = businessUserRepository.findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        businessUserRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    private void handleFailedLogin(BusinessUser user) {
        int attempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(attempts);

        // Lock account after 5 failed attempts for 30 minutes
        if (attempts >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("Account locked due to multiple failed login attempts: {}", user.getEmail());
        }

        businessUserRepository.save(user);
    }

    public void requestPasswordReset(String email) {
        BusinessUser user = businessUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate secure reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(System.currentTimeMillis() + 3600_000L); // 1 hour expiry
        businessUserRepository.save(user);

        // Send email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Click the link to reset your password: http://localhost:3000/reset-password?token=" + resetToken);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void confirmPasswordReset(String token, String newPassword) {
        BusinessUser user = businessUserRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry() < System.currentTimeMillis()) {
            throw new RuntimeException("Reset token expired");
        }

        // Update password (ensure password is hashed)
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        businessUserRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}