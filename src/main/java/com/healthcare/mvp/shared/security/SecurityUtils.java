package com.healthcare.mvp.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

}