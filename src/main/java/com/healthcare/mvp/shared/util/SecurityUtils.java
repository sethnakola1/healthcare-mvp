package com.healthcare.mvp.shared.util;

import com.healthcare.mvp.shared.security.AuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

public class SecurityUtils {

    /**
     * Get the current authenticated user ID
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return (String) principal;
        } else if (principal instanceof UserDetails) {
            // If using UserDetails, the username should be the business user ID
            return ((UserDetails) principal).getUsername();
        }

        return null;
    }

    /**
     * Get the current authenticated user email
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Extract email from authentication details
        Object details = authentication.getDetails();
        if (details instanceof AuthenticationDetails) {
            return ((AuthenticationDetails) details).getEmail();
        }

        // Fallback to authentication name
        return authentication.getName();
    }

    /**
     * Get the current authenticated user's hospital ID
     */
    public static String getCurrentUserHospitalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof AuthenticationDetails) {
            return ((AuthenticationDetails) details).getHospitalId();
        }

        return null;
    }

    /**
     * Get the current authenticated user's roles
     */
    public static java.util.List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Collections.emptyList();
        }

        Object details = authentication.getDetails();
        if (details instanceof AuthenticationDetails) {
            return ((AuthenticationDetails) details).getRoles();
        }

        return java.util.Collections.emptyList();
    }

    /**
     * Check if the current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Check if the current user is a Super Admin
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * Check if the current user is a Tech Advisor
     */
    public static boolean isTechAdvisor() {
        return hasRole("TECH_ADVISOR");
    }

    /**
     * Check if the current user is a Hospital Admin
     */
    public static boolean isHospitalAdmin() {
        return hasRole("HOSPITAL_ADMIN");
    }

    /**
     * Get current authentication object
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    /**
     * Get the current authenticated user ID as an Optional<UUID>
     * This is safer for auditing.
     */
    public static Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty(); // No user logged in
        }

        // The principal is the user ID (String) we set in JwtAuthenticationFilter
        String userId = (String) authentication.getPrincipal();
        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            // Log this error in a real application
            return Optional.empty(); // Invalid UUID format
        }
    }

    /**
     * Extracts the JWT from the Authorization header of the request.
     *
     * @param request The HTTP request.
     * @return The token string, or null if not found.
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Gets the client's IP address from the request.
     * It checks for the X-Forwarded-For header first, which is common for requests
     * coming through a proxy or load balancer.
     *
     * @param request The HTTP request.
     * @return The client's IP address.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr();
        }
        // Return the first IP in the X-Forwarded-For list
        return xfHeader.split(",")[0];
    }

    /**
     * Generates a simple device fingerprint from the User-Agent header.
     * For a real-world application, a more robust library like FingerprintJS would be used.
     *
     * @param request The HTTP request.
     * @return A string representing the device fingerprint.
     */
    public static String generateDeviceFingerprint(HttpServletRequest request) {
        // This is a very basic implementation.
        // A real implementation would involve hashing more details.
        return request.getHeader("User-Agent");
    }
}