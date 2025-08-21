package com.healthcare.mvp.business.service;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import com.healthcare.mvp.shared.constants.BusinessRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BusinessUserService {

    private final BusinessUserRepository businessUserRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final PartnerCodeService partnerCodeService;


    /**
     * Create initial Super Admin in database with FIXED password handling
     */
    public BusinessUserDto createInitialSuperAdmin() {
        // Check if super admin already exists
        Optional<BusinessUser> existingSuperAdmin = businessUserRepository.findByBusinessRole(BusinessRole.SUPER_ADMIN)
                .stream().findFirst();

        if (existingSuperAdmin.isPresent()) {
            BusinessUser admin = existingSuperAdmin.get();

            // Check if password needs fixing
            String testPassword = "SuperAdmin123!";
            boolean passwordValid = false;

            try {
                passwordValid = passwordEncoder.matches(testPassword, admin.getPasswordHash());
            } catch (Exception e) {
                log.warn("Invalid password hash detected for super admin, will reset it");
            }

            if (!passwordValid) {
                log.info("Fixing super admin password...");
                admin.setPasswordHash(passwordEncoder.encode(testPassword));
                admin.setLoginAttempts(0);
                admin.setAccountLockedUntil(null);
                admin.setUpdatedAt(LocalDateTime.now());

                // Set updated_by to self to avoid foreign key issues
                admin.setUpdatedBy(admin.getBusinessUserId());

                BusinessUser saved = businessUserRepository.save(admin);
                log.info("Super admin password fixed successfully");
                return convertToDto(saved);
            }

            return convertToDto(admin);
        }
        
        // Create first super admin: Sethna Kola
        BusinessUser superAdmin = new BusinessUser(
            "Sethna",
            "Kola",
            "sethna.kola@healthcareplatform.com",
            "sethna.superadmin",
            BusinessRole.SUPER_ADMIN,
                "sethnakola"
        );
        
        // Ensure proper BCrypt encoding
        String password = "SuperAdmin123!";
        String hashedPassword = passwordEncoder.encode(password);
        superAdmin.setPasswordHash(hashedPassword);

        // Verify the hash works immediately
        if (!passwordEncoder.matches(password, hashedPassword)) {
            throw new RuntimeException("Password encoding verification failed!");
        }

        superAdmin.setPartnerCode(partnerCodeService.generateBusinessUserCode(BusinessRole.SUPER_ADMIN));
        superAdmin.setTerritory("Global");
        superAdmin.setEmailVerified(true);
        superAdmin.setIsActive(true);
        superAdmin.setLoginAttempts(0);
        superAdmin.setCommissionPercentage(BigDecimal.ZERO);
        superAdmin.setTotalHospitalsBrought(0);
        superAdmin.setTotalCommissionEarned(BigDecimal.ZERO);

        BusinessUser saved = businessUserRepository.save(superAdmin);

        log.info("Super admin created with email: {} and password: {}",
                saved.getEmail(), password);
        log.info("Password hash: {}", saved.getPasswordHash());

        return convertToDto(saved);
    }
    
    /**
     * Create Tech Advisor by Super Admin
     */
    public BusinessUserDto createTechAdvisor(CreateBusinessUserRequest request) {
        // Validate email and username uniqueness
        if (businessUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        if (businessUserRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Create tech advisor
        BusinessUser techAdvisor = new BusinessUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getUsername(),
                BusinessRole.TECH_ADVISOR,
                request.getCognitoUserId() != null ? request.getCognitoUserId() : request.getUsername()
        );
        
        // Ensure proper password encoding
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        techAdvisor.setPasswordHash(hashedPassword);

        // Verify the hash works
        if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
            throw new RuntimeException("Password encoding verification failed!");
        }

        techAdvisor.setPhoneNumber(request.getPhoneNumber());
        techAdvisor.setTerritory(request.getTerritory());
        techAdvisor.setCommissionPercentage(request.getCommissionPercentage());
        techAdvisor.setTargetHospitalsMonthly(request.getTargetHospitalsMonthly());
        techAdvisor.setPartnerCode(partnerCodeService.generateBusinessUserCode(BusinessRole.TECH_ADVISOR));
        techAdvisor.setEmailVerified(true);
        techAdvisor.setIsActive(true);
        techAdvisor.setLoginAttempts(0);

        BusinessUser saved = businessUserRepository.save(techAdvisor);
        log.info("Tech advisor created: {} with properly encoded password", saved.getEmail());

        return convertToDto(saved);
    }

    /**
     * Create Super Admin (for registration)
     */
    public BusinessUserDto createSuperAdmin(CreateBusinessUserRequest request) {
        // Validate email and username uniqueness
        if (businessUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (businessUserRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create super admin
        BusinessUser superAdmin = new BusinessUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getUsername(),
                BusinessRole.SUPER_ADMIN,
                request.getCognitoUserId() != null ? request.getCognitoUserId() : ""
        );

        // Ensure proper password encoding
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        superAdmin.setPasswordHash(hashedPassword);

        // Verify the hash works
        if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
            throw new RuntimeException("Password encoding verification failed!");
        }

        superAdmin.setPhoneNumber(request.getPhoneNumber());
        superAdmin.setTerritory(request.getTerritory());
        superAdmin.setCommissionPercentage(BigDecimal.ZERO);
        superAdmin.setTargetHospitalsMonthly(0);
        superAdmin.setPartnerCode(partnerCodeService.generateBusinessUserCode(BusinessRole.SUPER_ADMIN));
        superAdmin.setEmailVerified(true);
        superAdmin.setIsActive(true);
        superAdmin.setLoginAttempts(0);

        BusinessUser saved = businessUserRepository.save(superAdmin);
        log.info("Super admin created: {} with properly encoded password", saved.getEmail());

        return convertToDto(saved);
    }
    
    /**
     * Get all Tech Advisors
     */
    public List<BusinessUserDto> getAllTechAdvisors() {
        return businessUserRepository.findActiveTechAdvisors()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get Business User by ID
     */
    public Optional<BusinessUserDto> getBusinessUserById(UUID businessUserId) {
        return businessUserRepository.findById(businessUserId)
                .map(this::convertToDto);
    }
    
    /**
     * Get Business User by Partner Code
     */
    public Optional<BusinessUserDto> getBusinessUserByPartnerCode(String partnerCode) {
        return businessUserRepository.findByPartnerCode(partnerCode)
                .map(this::convertToDto);
    }
    
    /**
     * Update Tech Advisor
     */
    public BusinessUserDto updateTechAdvisor(UUID techAdvisorId, CreateBusinessUserRequest request) {
        BusinessUser techAdvisor = businessUserRepository.findById(techAdvisorId)
                .orElseThrow(() -> new RuntimeException("Tech Advisor not found"));
        
        if (!techAdvisor.getBusinessRole().equals(BusinessRole.TECH_ADVISOR)) {
            throw new RuntimeException("User is not a Tech Advisor");
        }
        
        // Update fields
        techAdvisor.setFirstName(request.getFirstName());
        techAdvisor.setLastName(request.getLastName());
        techAdvisor.setPhoneNumber(request.getPhoneNumber());
        techAdvisor.setTerritory(request.getTerritory());
        techAdvisor.setCommissionPercentage(request.getCommissionPercentage());
        techAdvisor.setTargetHospitalsMonthly(request.getTargetHospitalsMonthly());
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            techAdvisor.setPasswordHash(hashedPassword);

            // Verify the hash works
            if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
                throw new RuntimeException("Password encoding verification failed!");
            }
        }
        
        BusinessUser saved = businessUserRepository.save(techAdvisor);
        return convertToDto(saved);
    }
    
    /**
     * Deactivate Tech Advisor
     */
    public void deactivateTechAdvisor(UUID techAdvisorId) {
        BusinessUser techAdvisor = businessUserRepository.findById(techAdvisorId)
                .orElseThrow(() -> new RuntimeException("Tech Advisor not found"));
        
        techAdvisor.setIsActive(false);
        businessUserRepository.save(techAdvisor);
    }

    /**
     * Check if email is already taken
     */
    public boolean isEmailTaken(String email) {
        return businessUserRepository.existsByEmail(email);
    }

    /**
     * Check if username is already taken
     */
    public boolean isUsernameTaken(String username) {
        return businessUserRepository.existsByUsername(username);
    }

    /**
     * Get all Super Admins
     */
    public List<BusinessUserDto> getAllSuperAdmins() {
        return businessUserRepository.findByBusinessRole(BusinessRole.SUPER_ADMIN)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user statistics
     */
    public UserStatsDto getUserStatistics() {
        long superAdminCount = businessUserRepository.countActiveUsersByRole(BusinessRole.SUPER_ADMIN);
        long techAdvisorCount = businessUserRepository.countActiveUsersByRole(BusinessRole.TECH_ADVISOR);
        long totalUsers = superAdminCount + techAdvisorCount;

        return UserStatsDto.builder()
                .totalUsers(totalUsers)
                .superAdminCount(superAdminCount)
                .techAdvisorCount(techAdvisorCount)
                .build();
    }

    /**
     * Get system metrics for SUPER_ADMIN dashboard
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", businessUserRepository.count());
//        metrics.put("activeUsers", businessUserRepository.countByIsActive(true));
        metrics.put("superAdminCount", businessUserRepository.countActiveUsersByRole(BusinessRole.SUPER_ADMIN));
        metrics.put("techAdvisorCount", businessUserRepository.countActiveUsersByRole(BusinessRole.TECH_ADVISOR));
        metrics.put("totalHospitals", hospitalRepository.count());
        metrics.put("activeHospitals", hospitalRepository.countByIsActive(true));
        return metrics;
    }

    /**
     * Get all users for SUPER_ADMIN dashboard
     */
    public List<BusinessUserDto> getAllUsers() {
        return businessUserRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public BusinessUserDto getUserById(UUID uuid) {
        return businessUserRepository.findById(uuid)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Reset user password (for admin/recovery purposes)
     */
    public void resetUserPassword(String email, String newPassword) {
        BusinessUser user = businessUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);

        businessUserRepository.save(user);
        log.info("Password reset for user: {}", email);
    }

    /**
     * Convert Entity to DTO
     */
    private BusinessUserDto convertToDto(BusinessUser businessUser) {
        BusinessUserDto dto = new BusinessUserDto();
        dto.setBusinessUserId(businessUser.getBusinessUserId());
        dto.setUsername(businessUser.getUsername());
        dto.setEmail(businessUser.getEmail());
        dto.setFirstName(businessUser.getFirstName());
        dto.setLastName(businessUser.getLastName());
        dto.setPhoneNumber(businessUser.getPhoneNumber());
        dto.setBusinessRole(businessUser.getBusinessRole());
        dto.setPartnerCode(businessUser.getPartnerCode());
        dto.setCommissionPercentage(businessUser.getCommissionPercentage());
        dto.setTerritory(businessUser.getTerritory());
        dto.setTargetHospitalsMonthly(businessUser.getTargetHospitalsMonthly());
        dto.setTotalHospitalsBrought(businessUser.getTotalHospitalsBrought());
        dto.setTotalCommissionEarned(businessUser.getTotalCommissionEarned());
        dto.setIsActive(businessUser.getIsActive());
        dto.setEmailVerified(businessUser.getEmailVerified());
        dto.setLastLogin(businessUser.getLastLogin());
        dto.setCreatedAt(businessUser.getCreatedAt());
        return dto;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserStatsDto {
        private long totalUsers;
        private long superAdminCount;
        private long techAdvisorCount;
    }
}