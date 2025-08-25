package com.healthcare.mvp.shared.security.service;

import com.healthcare.mvp.shared.security.entity.TokenBlocklist;
import com.healthcare.mvp.shared.security.repository.TokenBlocklistRepository;
import com.healthcare.mvp.shared.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlocklistService {
    
    private final TokenBlocklistRepository tokenBlocklistRepository;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public void blockToken(String token, String reason) {
        try {
            Claims claims = jwtUtil.getAllClaimsFromToken(token);
            String tokenId = claims.getId();
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            LocalDateTime expiresAt = claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            TokenBlocklist blockedToken = TokenBlocklist.builder()
                    .tokenId(tokenId)
                    .userId(userId)
                    .expiresAt(expiresAt)
                    .reason(reason)
                    .build();
            
            tokenBlocklistRepository.save(blockedToken);
            log.info("Blocked token for user: {} with reason: {}", userId, reason);
        } catch (Exception e) {
            log.error("Failed to block token: {}", e.getMessage());
        }
    }
    
    @Cacheable("blockedTokens")
    public boolean isTokenBlocked(String tokenId) {
        return tokenBlocklistRepository.existsByTokenIdAndExpiresAtAfter(
                tokenId, LocalDateTime.now());
    }
    
    @Transactional
    public void blockAllUserTokens(UUID userId, String reason) {
        // This would require tracking all active tokens for a user
        // For now, we'll just add a user-level block entry
        TokenBlocklist userBlock = TokenBlocklist.builder()
                .tokenId("USER_BLOCK_" + userId)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusDays(30)) // Block for 30 days
                .reason(reason)
                .build();
        
        tokenBlocklistRepository.save(userBlock);
        log.info("Blocked all tokens for user: {} with reason: {}", userId, reason);
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Async
    public void cleanupExpiredTokens() {
        int deleted = tokenBlocklistRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired blocked tokens", deleted);
    }
}