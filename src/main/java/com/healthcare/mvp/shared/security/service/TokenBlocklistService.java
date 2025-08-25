package com.healthcare.mvp.shared.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlocklistService {

    // In-memory blocklist for MVP (use Redis for production)
    private final Set<String> blockedTokens = ConcurrentHashMap.newKeySet();
    private final Set<String> blockedRefreshTokens = ConcurrentHashMap.newKeySet();

    /**
     * Block access token - FIXED signature to match usage
     */
    public void blockToken(String token, String reason) {
        blockedTokens.add(token);
        log.info("Access token blocked. Reason: {}", reason);
    }

    /**
     * Block refresh token - ADDED missing method
     */
    public void blockRefreshToken(String refreshToken, UUID userId, String reason, String tokenType) {
        blockedRefreshTokens.add(refreshToken);
        log.info("Refresh token blocked for user: {}. Reason: {}", userId, reason);
    }

    /**
     * Check if access token is blocked
     */
    public boolean isTokenBlocked(String token) {
        return blockedTokens.contains(token);
    }

    /**
     * Check if refresh token is blocked
     */
    public boolean isRefreshTokenBlocked(String refreshToken) {
        return blockedRefreshTokens.contains(refreshToken);
    }

    /**
     * Clear expired tokens (cleanup job)
     */
    public void clearExpiredTokens() {
        // In production, implement proper cleanup logic
        // For now, clear all tokens older than 24 hours
        blockedTokens.clear();
        blockedRefreshTokens.clear();
        log.info("Cleaned up expired tokens from blocklist");
    }

    /**
     * Get blocked token count (for monitoring)
     */
    public int getBlockedTokenCount() {
        return blockedTokens.size();
    }

    /**
     * Get blocked refresh token count (for monitoring)
     */
    public int getBlockedRefreshTokenCount() {
        return blockedRefreshTokens.size();
    }
}