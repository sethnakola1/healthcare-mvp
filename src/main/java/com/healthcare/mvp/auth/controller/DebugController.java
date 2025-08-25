// Add this temporary controller to debug and fix the password issue

package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DebugController {
    
    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Comprehensive fix for the super admin account
     */
    @PostMapping("/fix-super-admin-complete")
    @Transactional
    public ResponseEntity<BaseResponse<Map<String, Object>>> fixSuperAdminComplete() {
        Map<String, Object> result = new HashMap<>();

        try {
            String email = "sethnakola@healthhorizon.com";
            String newPassword = "SuperAdmin123!";

            log.info("=== STARTING COMPREHENSIVE SUPER ADMIN FIX ===");

            // Step 1: Check current state
            log.info("Step 1: Checking current user state");
            List<BusinessUser> users = businessUserRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .toList();

            result.put("usersFoundWithEmail", users.size());

            if (users.isEmpty()) {
                log.error("No user found with email: {}", email);
                result.put("error", "User not found");
                return ResponseEntity.ok(BaseResponse.error("User not found", newPassword));
            }

            if (users.size() > 1) {
                log.warn("Multiple users found with same email! Count: {}", users.size());
                result.put("warning", "Multiple users with same email");
            }

            BusinessUser user = users.get(0);

            // Log current state
            log.info("Current user state:");
            log.info("  - ID: {}", user.getBusinessUserId());
            log.info("  - Name: {} {}", user.getFirstName(), user.getLastName());
            log.info("  - Role: {}", user.getBusinessRole());
            log.info("  - Active: {}", user.getIsActive());
            log.info("  - Email Verified: {}", user.getEmailVerified());
            log.info("  - Login Attempts: {}", user.getLoginAttempts());

            result.put("currentFirstName", user.getFirstName());
            result.put("currentLastName", user.getLastName());

            // Step 2: Fix user data
            log.info("Step 2: Updating user data");
            user.setFirstName("Sethna");
            user.setLastName("Kola");
            user.setIsActive(true);
            user.setEmailVerified(true);
            user.setLoginAttempts(0);
            user.setAccountLockedUntil(null);

            // Step 3: Generate and verify new password hash
            log.info("Step 3: Generating new password hash");
            String newHash = passwordEncoder.encode(newPassword);

            // Immediately verify it works
            boolean hashVerifies = passwordEncoder.matches(newPassword, newHash);
            log.info("New hash verification: {}", hashVerifies);

            if (!hashVerifies) {
                log.error("Generated hash doesn't verify!");
                result.put("error", "Password encoding failed");
                return ResponseEntity.ok(BaseResponse.error("Password encoding failed", newHash));
            }

            user.setPasswordHash(newHash);
            user.setUpdatedAt(LocalDateTime.now());

            // Set updated_by to self to avoid foreign key issues
            if (user.getBusinessUserId() != null) {
                user.setUpdatedBy(user.getBusinessUserId());
            }

            // Step 4: Save the user
            log.info("Step 4: Saving user");
            BusinessUser savedUser = businessUserRepository.save(user);

            // Step 5: Verify the save worked
            log.info("Step 5: Verifying saved data");
            BusinessUser verifyUser = businessUserRepository.findByEmail(email)
                .orElse(null);

            if (verifyUser == null) {
                log.error("User not found after save!");
                result.put("error", "Save verification failed");
                return ResponseEntity.ok(BaseResponse.error("Save verification failed", newHash));
            }

            // Verify password works on saved user
            boolean savedHashWorks = passwordEncoder.matches(newPassword, verifyUser.getPasswordHash());
            log.info("Saved hash verification: {}", savedHashWorks);

            // Step 6: Test login simulation
            log.info("Step 6: Simulating login");
            boolean loginWouldWork = testLoginSimulation(email, newPassword);

            // Prepare result
            result.put("email", email);
            result.put("newPassword", newPassword);
            result.put("userId", savedUser.getBusinessUserId().toString());
            result.put("firstName", savedUser.getFirstName());
            result.put("lastName", savedUser.getLastName());
            result.put("role", savedUser.getBusinessRole().toString());
            result.put("isActive", savedUser.getIsActive());
            result.put("emailVerified", savedUser.getEmailVerified());
            result.put("newHashVerified", hashVerifies);
            result.put("savedHashVerified", savedHashWorks);
            result.put("loginSimulationSuccess", loginWouldWork);
            result.put("hashPrefix", newHash.substring(0, 10) + "...");

            log.info("=== SUPER ADMIN FIX COMPLETE ===");
            log.info("User can now login with email: {} and password: {}", email, newPassword);

            return ResponseEntity.ok(BaseResponse.success(
                "Super admin fixed successfully. Login with: " + email + " / " + newPassword,
                result));

        } catch (Exception e) {
            log.error("Failed to fix super admin", e);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.ok(BaseResponse.error("Fix failed: " + e.getMessage()));
        }
    }

    /**
     * Simulate login to test if it would work
     */
    private boolean testLoginSimulation(String email, String password) {
        try {
            log.debug("Testing login simulation for: {}", email);
            
            BusinessUser user = businessUserRepository.findByEmail(email)
                .orElse(null);
            
            if (user == null) {
                log.debug("Simulation: User not found");
                return false;
            }
            
            if (!user.getIsActive()) {
                log.debug("Simulation: User not active");
                return false;
            }
            
            if (!user.getEmailVerified()) {
                log.debug("Simulation: Email not verified");
                return false;
            }

            boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
            log.debug("Simulation: Password matches: {}", passwordMatches);
            
            return passwordMatches;
            
        } catch (Exception e) {
            log.error("Login simulation failed", e);
            return false;
        }
    }

    /**
     * Get detailed diagnostics
     */
    @GetMapping("/diagnostics")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        try {
            // Check database connection
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();
            diagnostics.put("databaseConnection", "OK");

            // Check user count
            long userCount = businessUserRepository.count();
            diagnostics.put("totalUsers", userCount);

            // Check super admin
            List<BusinessUser> superAdmins = businessUserRepository.findAll().stream()
                .filter(u -> u.getBusinessRole().toString().equals("SUPER_ADMIN"))
                .toList();

            diagnostics.put("superAdminCount", superAdmins.size());

            // Check password encoder
            String testPassword = "test123";
            String testHash = passwordEncoder.encode(testPassword);
            boolean encoderWorks = passwordEncoder.matches(testPassword, testHash);
            diagnostics.put("passwordEncoderWorks", encoderWorks);

            // List all users (without sensitive data)
            List<Map<String, Object>> userList = businessUserRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", u.getBusinessUserId());
                    userInfo.put("email", u.getEmail());
                    userInfo.put("name", u.getFirstName() + " " + u.getLastName());
                    userInfo.put("role", u.getBusinessRole());
                    userInfo.put("active", u.getIsActive());
                    userInfo.put("hashValid", u.getPasswordHash() != null &&
                                            u.getPasswordHash().startsWith("$2"));
                    return userInfo;
                })
                .toList();

            diagnostics.put("users", userList);

            return ResponseEntity.ok(BaseResponse.success("Diagnostics retrieved", diagnostics));

        } catch (Exception e) {
            log.error("Diagnostics failed", e);
            diagnostics.put("error", e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("Diagnostics failed: " + e.getMessage()));
        }
    }
    
    /**
     * Test password encoding directly
     */
    @PostMapping("/test-password")
    public ResponseEntity<BaseResponse<Map<String, Object>>> testPassword(
            @RequestParam String password) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Generate multiple hashes to show they're different but all work
            String hash1 = passwordEncoder.encode(password);
            String hash2 = passwordEncoder.encode(password);
            String hash3 = passwordEncoder.encode(password);

            result.put("password", password);
            result.put("hash1", hash1);
            result.put("hash2", hash2);
            result.put("hash3", hash3);
            result.put("allDifferent", !hash1.equals(hash2) && !hash2.equals(hash3));
            result.put("hash1Verifies", passwordEncoder.matches(password, hash1));
            result.put("hash2Verifies", passwordEncoder.matches(password, hash2));
            result.put("hash3Verifies", passwordEncoder.matches(password, hash3));
            
            // Test with the super admin
            BusinessUser user = businessUserRepository.findByEmail("sethnakola@healthhorizon.com")
                .orElse(null);
            
            if (user != null) {
                boolean matchesCurrent = passwordEncoder.matches(password, user.getPasswordHash());
                result.put("matchesCurrentUserHash", matchesCurrent);
            }
            
            return ResponseEntity.ok(BaseResponse.success("Password test completed", result));
            
        } catch (Exception e) {
            log.error("Password test failed", e);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("Test failed: " + e.getMessage(), password));
        }
    }
}