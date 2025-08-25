package com.healthcare.mvp.shared.security.service;

import com.healthcare.mvp.shared.exception.ResourceNotFoundException;
import com.healthcare.mvp.shared.security.entity.RefreshToken;
import com.healthcare.mvp.shared.security.repository.RefreshTokenRepository;
import com.healthcare.mvp.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    
    @Value("${healthcare.app.jwtRefreshExpirationMs}")
    private long refreshTokenExpirationMs;
    
    @Value("${healthcare.app.maxRefreshTokensPerUser}")
    private int maxRefreshTokensPerUser = 5;
    
    @Transactional
    public RefreshToken createRefreshToken(UUID userId, String deviceFingerprint, String ipAddress) {
        // Clean up old tokens for this user if exceeding limit
        cleanupExcessTokensForUser(userId);
        
        String familyId = UUID.randomUUID().toString();
        String tokenValue = jwtUtil.generateRefreshToken(userId, familyId);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .deviceFingerprint(deviceFingerprint)
                .ipAddress(ipAddress)
                .familyId(familyId)
                .build();
        
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {} from IP: {}", userId, ipAddress);
        return saved;
    }
    
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String deviceFingerprint, String ipAddress) {
        // Validate the old token
        if (oldToken.isExpired() || oldToken.getRevoked()) {
            // Check for token reuse attack
            if (oldToken.getRevoked()) {
                log.warn("Attempt to reuse revoked refresh token. Revoking entire family: {}", oldToken.getFamilyId());
                refreshTokenRepository.revokeTokenFamily(oldToken.getFamilyId());
                throw new SecurityException("Token reuse detected. All tokens in family revoked.");
            }
            throw new SecurityException("Refresh token is expired");
        }
        
        // Create hash of old token for tracking
        String oldTokenHash = hashToken(oldToken.getToken());
        
        // Generate new token with same family ID
        String newTokenValue = jwtUtil.generateRefreshToken(oldToken.getUserId(), oldToken.getFamilyId());
        
        RefreshToken newToken = RefreshToken.builder()
                .token(newTokenValue)
                .userId(oldToken.getUserId())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .deviceFingerprint(deviceFingerprint)
                .ipAddress(ipAddress)
                .familyId(oldToken.getFamilyId())
                .previousTokenHash(oldTokenHash)
                .build();
        
        // Revoke the old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        
        RefreshToken saved = refreshTokenRepository.save(newToken);
        log.info("Rotated refresh token for user: {} from IP: {}", oldToken.getUserId(), ipAddress);
        return saved;
    }
    
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
    }
    
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Revoked refresh token for user: {}", refreshToken.getUserId());
    }
    
    @Transactional
    public void revokeAllTokensForUser(UUID userId) {
        refreshTokenRepository.revokeAllTokensByUserId(userId);
        log.info("Revoked all refresh tokens for user: {}", userId);
    }
    
    private void cleanupExcessTokensForUser(UUID userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findActiveTokensByUserId(userId, LocalDateTime.now());
        
        if (activeTokens.size() >= maxRefreshTokensPerUser) {
            // Remove oldest tokens
            activeTokens.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            List<RefreshToken> tokensToRevoke = activeTokens.subList(0, 
                    activeTokens.size() - maxRefreshTokensPerUser + 1);
            
            tokensToRevoke.forEach(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
            
            log.info("Cleaned up {} excess tokens for user: {}", tokensToRevoke.size(), userId);
        }
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Async
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}