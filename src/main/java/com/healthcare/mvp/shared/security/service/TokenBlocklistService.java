package com.healthcare.mvp.shared.security.service;

import com.healthcare.mvp.shared.security.entity.TokenBlocklist;
import com.healthcare.mvp.shared.security.repository.TokenBlocklistRepository;
import com.healthcare.mvp.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenBlocklistService {

    private final TokenBlocklistRepository tokenBlocklistRepository;
    private final JwtUtil jwtUtil;

    /**
     * Block a token by extracting its details and adding it to the blocklist.
     *
     * @param token  The JWT to block.
     * @param reason The reason for blocking the token.
     */
    public void blockToken(String token, String reason) {
        try {
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            String userEmail = jwtUtil.getEmailFromToken(token);
            Date expiry = jwtUtil.getExpirationDateFromToken(token);

            if (userIdStr == null || userEmail == null) {
                log.warn("Cannot block token with missing user details.");
                return;
            }

            // If expiry is null, the token might be invalid or non-expiring.
            // For security, we can block it indefinitely or for a short period.
            // Here, we will not block it if expiry is not present.
            if (expiry == null) {
                log.warn("Cannot block token without an expiry date for user: {}", userEmail);
                return;
            }

            UUID userId = UUID.fromString(userIdStr);
            String tokenHash = hashToken(token);

            if (tokenBlocklistRepository.existsByTokenHash(tokenHash)) {
                log.debug("Token already blocked for user: {}", userEmail);
                return;
            }

            TokenBlocklist blockedToken = TokenBlocklist.builder()
                    .tokenHash(tokenHash)
                    .userId(userId)
                    .userEmail(userEmail)
                    .tokenType(TokenBlocklist.TokenType.ACCESS_TOKEN)
                    .reason(reason)
                    .expiryDate(convertToLocalDateTime(expiry))
                    .isActive(true)
                    .createdBy(userId)
                    .build();

            tokenBlocklistRepository.save(blockedToken);
            log.info("Token blocked for user: {} - Reason: {}", userEmail, reason);

        } catch (Exception e) {
            log.error("Failed to block token. Error: {}", e.getMessage(), e);
            // Do not rethrow, as failure to block a token should not break the user flow (e.g., logout).
        }
    }

    /**
     * Check if a token is blocked
     */
    public boolean isTokenBlocked(String token) {
        try {
            String tokenHash = hashToken(token);
            return tokenBlocklistRepository.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            log.error("Error checking token blocklist: {}", e.getMessage(), e);
            // In case of error, treat as blocked for security
            return true;
        }
    }

    /**
     * Block all tokens for a user (for security incidents)
     */
    public void blockAllUserTokens(UUID userId, String reason) {
        try {
            List<TokenBlocklist> userTokens = tokenBlocklistRepository.findActiveTokensByUserId(userId);
            userTokens.forEach(token -> {
                token.deactivate();
                token.setReason(reason + " - Mass token revocation");
            });

            tokenBlocklistRepository.saveAll(userTokens);
            log.warn("All tokens blocked for user: {} - Reason: {}", userId, reason);

        } catch (Exception e) {
            log.error("Failed to block all tokens for user: {}", userId, e);
        }
    }

    /**
     * Cleanup expired tokens (scheduled task)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = tokenBlocklistRepository.deleteExpiredTokens(LocalDateTime.now());
            if (deletedCount > 0) {
                log.info("Cleanup completed. Deleted {} expired blocked tokens.", deletedCount);
            } else {
                log.info("No expired tokens to clean up.");
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }

    /**
     * Get blocklist statistics
     */
    public BlocklistStats getBlocklistStats() {
        try {
            long totalBlocked = tokenBlocklistRepository.countByIsActive(true);
            return new BlocklistStats(totalBlocked);
        } catch (Exception e) {
            log.error("Failed to get blocklist stats", e);
            return new BlocklistStats(0);
        }
    }

    // Helper methods
    private String hashToken(String token) {
        if (token == null) return null;
        return DigestUtils.md5DigestAsHex(token.getBytes());
    }

    private LocalDateTime convertToLocalDateTime(java.util.Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // Statistics DTO
    public static class BlocklistStats {
        private final long totalBlockedTokens;

        public BlocklistStats(long totalBlockedTokens) {
            this.totalBlockedTokens = totalBlockedTokens;
        }

        public long getTotalBlockedTokens() {
            return totalBlockedTokens;
        }
    }
}