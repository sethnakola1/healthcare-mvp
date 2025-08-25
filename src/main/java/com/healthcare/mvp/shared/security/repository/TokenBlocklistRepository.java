package com.healthcare.mvp.shared.security.repository;

import com.healthcare.mvp.shared.security.entity.TokenBlocklist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenBlocklistRepository extends JpaRepository<TokenBlocklist, UUID> {

    /**
     * Check if a token is blacklisted
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Find token by hash
     */
    Optional<TokenBlocklist> findByTokenHash(String tokenHash);

    /**
     * Delete expired tokens (cleanup job)
     *
     * @return number of expired tokens delete
     */
//    @Query("DELETE FROM TokenBlocklist t WHERE t.expiryDate < :currentTime")
//    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenBlocklist t WHERE t.expiryDate < :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count blacklisted tokens
     */
    long countByIsActive(boolean isActive);

    /**
     * Find tokens by user ID
     */
    @Query("SELECT t FROM TokenBlocklist t WHERE t.userId = :userId AND t.isActive = true")
    java.util.List<TokenBlocklist> findActiveTokensByUserId(@Param("userId") UUID userId);


}