package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.auth.dto.LoginRequest;
import com.healthcare.mvp.auth.dto.LoginResponse;
import com.healthcare.mvp.auth.dto.RefreshTokenRequest;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import com.healthcare.mvp.shared.util.JwtUtil;
import com.healthcare.mvp.shared.security.SecurityUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
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
            // Step 1: Find user
            log.debug("Step 1: Looking up user by email: {}", request.getEmail());
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

            // Step 2: Check if account is active
            log.debug("Step 2: Checking if account is active: {}", user.getIsActive());
            if (!user.getIsActive()) {
                log.warn("Login attempt for inactive account: {}", request.getEmail());
                throw new AuthenticationException("Account is deactivated. Please contact support.");
            }

            // Step 3: Check if account is locked
            log.debug("Step 3: Checking account lock status");
            if (user.getAccountLockedUntil() != null &&
                user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                log.warn("Login attempt for locked account: {} (locked until: {})",
                        request.getEmail(), user.getAccountLockedUntil());
                throw new AuthenticationException("Account is temporarily locked. Please try again later.");
            }

            // Step 4: Validate password
            log.debug("Step 4: Validating password");
            String providedPassword = request.getPassword();
            String storedHash = user.getPasswordHash();

            // Log hash prefix for debugging (safe to log first few chars)
            log.debug("Stored hash prefix: {}",
                    storedHash != null && storedHash.length() > 10 ?
                    storedHash.substring(0, 10) + "..." : "NULL or SHORT");

            // Check if hash is in BCrypt format
            if (storedHash == null || !storedHash.startsWith("$2a$") && !storedHash.startsWith("$2b$")) {
                log.error("Invalid password hash format for user: {}. Hash prefix: {}",
                        request.getEmail(),
                        storedHash != null && storedHash.length() > 3 ? storedHash.substring(0, 3) : "NULL");
                throw new AuthenticationException("Account password configuration error. Please contact support.");
            }

            boolean passwordMatches = false;
            try {
                passwordMatches = passwordEncoder.matches(providedPassword, storedHash);
                log.debug("Password match result: {}", passwordMatches);
            } catch (Exception e) {
                log.error("Error during password verification for user {}: {}",
                        request.getEmail(), e.getMessage(), e);
                throw new AuthenticationException("Password verification failed. Please contact support.");
            }

            if (!passwordMatches) {
                log.warn("Invalid password for user: {}", request.getEmail());
                handleFailedLogin(user);
                throw new AuthenticationException("Invalid email or password");
            }

            // Step 5: Reset login attempts and update last login
            log.debug("Step 5: Updating login success data");
            user.setLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            businessUserRepository.save(user);

            // Step 6: Generate tokens
            log.debug("Step 6: Generating JWT tokens");
            String role = user.getBusinessRole().name();
            String accessToken = jwtUtil.generateToken(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                null,  // hospitalId - will be null for SUPER_ADMIN
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            String refreshToken = jwtUtil.generateRefreshToken(user.getBusinessUserId().toString());

            log.info("=== LOGIN SUCCESS ===");
            log.info("Successful login for user: {} with role: {}", request.getEmail(), role);

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

        } catch (AuthenticationException e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Authentication failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== LOGIN ERROR ===");
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);

            // Check for specific database errors
            if (e.getMessage() != null && e.getMessage().contains("connection")) {
                throw new AuthenticationException("Database connection error. Please try again.");
            }

            throw new AuthenticationException("Login failed. Please try again.");
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        try {
            if (!jwtUtil.validateToken(request.getRefreshToken())) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String userId = jwtUtil.getUserIdFromToken(request.getRefreshToken());
            BusinessUser user = businessUserRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.getIsActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            String role = user.getBusinessRole().name();
            String accessToken = jwtUtil.generateToken(
                user.getBusinessUserId().toString(),
                user.getEmail(),
                null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            log.info("Token refreshed successfully for user: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(86400L)
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
//        user.setResetToken(resetToken);
//        user.setResetTokenExpiry(System.currentTimeMillis() + 3600_000); // 1 hour expiry
        businessUserRepository.save(user);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Click the link to reset your password: http://localhost:3000/reset-password?token=" + resetToken);
        mailSender.send(message);
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
        businessUserRepository.save(user);
    }

}