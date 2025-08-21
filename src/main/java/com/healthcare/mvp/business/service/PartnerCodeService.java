package com.healthcare.mvp.business.service;

import com.healthcare.mvp.business.repository.BusinessUserRepository;
import com.healthcare.mvp.shared.constants.BusinessRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PartnerCodeService {

    private final BusinessUserRepository businessUserRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate unique partner code for business user
     */
    public String generateBusinessUserCode(BusinessRole role) {
        String prefix = getRolePrefix(role);
        String uniqueCode;

        do {
            uniqueCode = prefix + generateRandomCode(6);
        } while (businessUserRepository.existsByPartnerCode(uniqueCode));

        return uniqueCode;
    }

    /**
     * Generate hospital partner code
     */
    public String generateHospitalCode(String hospitalName) {
        String prefix = "HSP";
        String nameCode = extractNameCode(hospitalName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd"));
        String randomPart = generateRandomCode(3);

        String baseCode = prefix + nameCode + timestamp + randomPart;
        String uniqueCode = baseCode;
        int counter = 1;

        // Ensure uniqueness (you'll need to check against hospital repository)
        while (isCodeTaken(uniqueCode)) {
            uniqueCode = baseCode + String.format("%02d", counter);
            counter++;
        }

        return uniqueCode;
    }

    /**
     * Generate appointment reference code
     */
    public String generateAppointmentCode() {
        String prefix = "APT";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = generateRandomCode(4);

        return prefix + timestamp + randomPart;
    }

    /**
     * Get role-specific prefix
     */
    private String getRolePrefix(BusinessRole role) {
        return switch (role) {
            case SUPER_ADMIN -> "SA";
            case TECH_ADVISOR -> "TA";
            case HOSPITAL_ADMIN -> "HA";
            case DOCTOR -> "DR";
            case NURSE -> "NR";
            case RECEPTIONIST -> "RC";
            case PATIENT -> "PT";
        };
    }

    /**
     * Extract meaningful code from hospital name
     */
    private String extractNameCode(String hospitalName) {
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            return "HSP";
        }

        String cleaned = hospitalName.toUpperCase()
                .replaceAll("[^A-Z\\s]", "")
                .trim();

        String[] words = cleaned.split("\\s+");
        StringBuilder code = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                code.append(word.charAt(0));
                if (code.length() >= 3) break;
            }
        }

        // Pad with 'X' if needed
        while (code.length() < 3) {
            code.append('X');
        }

        return code.toString();
    }

    /**
     * Generate random alphanumeric code
     */
    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * Check if code is already taken (placeholder - implement based on your needs)
     */
    private boolean isCodeTaken(String code) {
        // For business users
        if (businessUserRepository.existsByPartnerCode(code)) {
            return true;
        }

        // Add checks for hospital codes, appointment codes, etc. as needed
        return false;
    }

    /**
     * Validate partner code format
     */
    public boolean isValidPartnerCode(String code) {
        if (code == null || code.length() < 8) {
            return false;
        }

        return code.matches("^[A-Z]{2}[A-Z0-9]{6,}$");
    }
}