package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.auth.dto.*;
import com.healthcare.mvp.auth.service.AuthenticationService;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.service.RefreshTokenService;
import com.healthcare.mvp.shared.security.service.TokenBlocklistService;
import com.healthcare.mvp.shared.util.JwtUtil;
import com.healthcare.mvp.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlocklistService tokenBlocklistService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {


            // Rate limiting and security logging would be implemented here
            String ipAddress = SecurityUtils.getClientIpAddress(request);
            String deviceFingerprint = SecurityUtils.generateDeviceFingerprint(request);

            log.info("Login attempt for user: {} from IP: {}", loginRequest.getUsername(), ipAddress);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Assuming you have a method to get user info
            UUID userId = getUserIdFromUserDetails(userDetails);
            String role = getUserRoleFromUserDetails(userDetails);

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(userDetails, userId, role);
            var refreshToken = refreshTokenService.createRefreshToken(userId, deviceFingerprint, ipAddress);

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtExpirationMs())
                    .user(UserInfo.builder()
                            .id(userId)
                            .username(userDetails.getUsername())
                            .role(role)
                            .build())
                    .build();

            log.info("User {} logged in successfully from IP: {}", loginRequest.getUsername(), ipAddress);

            return ResponseEntity.ok(BaseResponse.success(response));


    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<RefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest,
            HttpServletRequest request) {

        try {
            String ipAddress = SecurityUtils.getClientIpAddress(request);
            String deviceFingerprint = SecurityUtils.generateDeviceFingerprint(request);

            var oldRefreshToken = refreshTokenService.findByToken(refreshRequest.getRefreshToken());

            // Rotate the refresh token
            var newRefreshToken = refreshTokenService.rotateRefreshToken(
                    oldRefreshToken, deviceFingerprint, ipAddress);

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(
                    loadUserFromToken(oldRefreshToken.getUserId()),
                    oldRefreshToken.getUserId(),
                    getUserRole(oldRefreshToken.getUserId()));

            RefreshResponse response = RefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtExpirationMs())
                    .build();

            return ResponseEntity.ok(BaseResponse.success(response));

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("Invalid refresh token", 401));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Object>> logout(
            HttpServletRequest request) {

        try {
            String token = SecurityUtils.extractTokenFromRequest(request);
            UUID userId = UUID.fromString(jwtUtil.getUserIdFromToken(token));

            // Block the current access token
            tokenBlocklistService.blockToken(token, "logout");

            // Revoke all refresh tokens for the user
            refreshTokenService.revokeAllTokensForUser(userId);

            log.info("User {} logged out successfully", userId);

            return ResponseEntity.ok(BaseResponse.success("Logged out successfully"));

        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Logout failed", 400));
        }
    }

    // Helper methods would be implemented here
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // Implementation depends on your UserDetails implementation
        return UUID.randomUUID(); // Placeholder
    }

    private String getUserRoleFromUserDetails(UserDetails userDetails) {
        // Implementation depends on your UserDetails implementation
        return "USER"; // Placeholder
    }

    private UserDetails loadUserFromToken(UUID userId) {
        // Load user details by ID
        return null; // Placeholder
    }

    // DTOs
    @lombok.Data
    public static class PasswordResetRequest {
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Please provide a valid email address")
        private String email;
    }
}