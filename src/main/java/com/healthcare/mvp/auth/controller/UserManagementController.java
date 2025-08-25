package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.constants.BusinessRole;
import com.healthcare.mvp.shared.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Registration", description = "Business user registration")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final BusinessUserService businessUserService;

    /**
     * Register a new business user (Super Admin or Tech Advisor)
     */
    @PostMapping("/register")
    @Operation(summary = "Register Business User", description = "Register a new Super Admin or Tech Advisor")
    public ResponseEntity<BaseResponse<BusinessUserDto>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registration request received for email: {} with role: {}", request.getEmail(), request.getRole());
        
        try {
            // Convert to CreateBusinessUserRequest
            CreateBusinessUserRequest createRequest = CreateBusinessUserRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .phoneNumber(request.getPhoneNumber())
                    .territory(request.getTerritory())
                    .commissionPercentage(request.getRole() == BusinessRole.SUPER_ADMIN ? 
                            BigDecimal.ZERO : new BigDecimal("20.00"))
                    .targetHospitalsMonthly(request.getRole() == BusinessRole.SUPER_ADMIN ? 0 : 5)
                    .cognitoUserId("") // Will be set later if needed
                    .build();

            BusinessUserDto user;
            if (request.getRole() == BusinessRole.SUPER_ADMIN) {
                user = businessUserService.createSuperAdmin(createRequest);
            } else if (request.getRole() == BusinessRole.TECH_ADVISOR) {
                user = businessUserService.createTechAdvisor(createRequest);
            } else {
                throw new RuntimeException("Invalid role for registration: " + request.getRole());
            }

            log.info("User registered successfully: {} with role: {}", request.getEmail(), request.getRole());
            return ResponseEntity.ok(BaseResponse.success("User registered successfully", user));

        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Get available business roles for registration
     */
    @GetMapping("/registration/roles")
    @Operation(summary = "Get Available Roles", description = "Get list of roles available for registration")
    public ResponseEntity<BaseResponse<List<BusinessRoleDto>>> getAvailableRoles() {
        List<BusinessRoleDto> roles = Arrays.asList(
                new BusinessRoleDto(BusinessRole.SUPER_ADMIN, "Super Admin", "Full system access and management"),
                new BusinessRoleDto(BusinessRole.TECH_ADVISOR, "Tech Advisor", "Technical advisor for hospital partnerships")
        );
        
        return ResponseEntity.ok(BaseResponse.success("Available roles retrieved", roles));
    }

    /**
     * Check if email/username is available
     */
    @GetMapping("/registration/check-availability")
    @Operation(summary = "Check Availability", description = "Check if email or username is available")
    public ResponseEntity<BaseResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username) {
        
        AvailabilityResponse response = new AvailabilityResponse();
        
        if (email != null) {
            response.setEmailAvailable(!businessUserService.isEmailTaken(email));
        }
        
        if (username != null) {
            response.setUsernameAvailable(!businessUserService.isUsernameTaken(username));
        }
        
        return ResponseEntity.ok(BaseResponse.success("Availability checked", response));
    }

    // DTOs for registration
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @PasswordMatches
    public static class UserRegistrationRequest {
        @jakarta.validation.constraints.NotBlank(message = "First name is required")
        private String firstName;

        @jakarta.validation.constraints.NotBlank(message = "Last name is required")
        private String lastName;

        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Please provide a valid email address")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters long")
        private String password;

        @jakarta.validation.constraints.NotBlank(message = "Confirm password is required")
        private String confirmPassword;

        private String phoneNumber;

        @jakarta.validation.constraints.NotBlank(message = "Territory is required")
        private String territory;

        @jakarta.validation.constraints.NotNull(message = "Role is required")
        private BusinessRole role;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BusinessRoleDto {
        private BusinessRole value;
        private String label;
        private String description;
    }

    @lombok.Data
    public static class AvailabilityResponse {
        private Boolean emailAvailable;
        private Boolean usernameAvailable;
    }
}