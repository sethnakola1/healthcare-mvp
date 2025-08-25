package com.healthcare.mvp.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.service.TokenBlocklistService;
import com.healthcare.mvp.shared.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced JWT Authentication Filter with Token Blocklist Support
 *
 * This filter:
 * 1. Extracts JWT tokens from Authorization header
 * 2. Validates token format and signature
 * 3. Checks if token is blocklisted (revoked)
 * 4. Sets Spring Security authentication context
 * 5. Handles authentication errors gracefully
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenBlocklistService tokenBlocklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Check if token is blocklisted first (performance optimization)
                if (tokenBlocklistService.isTokenBlocked(jwt)) {
                    log.warn("Attempted use of blocked token from IP: {}", getClientIpAddress(request));
                    handleAuthenticationError(response, "Token has been revoked");
                    return;
                }

                // Validate token signature and expiration
                if (jwtUtil.validateToken(jwt)) {
                    String userId = jwtUtil.getUserIdFromToken(jwt);
                    String email = jwtUtil.getEmailFromToken(jwt);
                    String hospitalId = jwtUtil.getHospitalIdFromToken(jwt);
                    List<String> roles = jwtUtil.getRolesFromToken(jwt);

                    // Create authorities list
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    // Set authentication details
                    AuthenticationDetails details = new AuthenticationDetails(userId, email, hospitalId, roles);
                    authentication.setDetails(details);

                    // Set in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Successfully authenticated user: {} with roles: {}", email, roles);
                } else {
                    log.debug("Invalid JWT token provided");
                    handleAuthenticationError(response, "Invalid or expired token");
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
            
            // Only send error response if a token was provided but failed
            if (getJwtFromRequest(request) != null) {
                handleAuthenticationError(response, "Authentication failed");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Handle authentication errors with consistent JSON responses
     */
    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse<Object> errorResponse = BaseResponse.builder()
                .success(false)
                .errorCode("AUTHENTICATION_FAILED")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Get client IP address from request (considering proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Determine if the filter should not be applied to this request
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip authentication for public endpoints
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/health") ||
               path.startsWith("/api/auth/reset-password") ||
               path.startsWith("/api/auth/confirm-reset") ||
               path.startsWith("/api/business/super-admin/initialize") ||
               path.startsWith("/api/debug/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars/") ||
               path.equals("/error");
    }
}