package com.healthcare.mvp.auth.service;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.business.repository.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final BusinessUserRepository businessUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void resetUserPassword(String email, String newPassword) {
        BusinessUser user = businessUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        businessUserRepository.save(user);
        log.info("Password reset for user: {}", email);
    }
}