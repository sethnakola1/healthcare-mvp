package com.healthcare.mvp.shared.security.repository;

import com.healthcare.mvp.shared.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByToken(String token);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllTokensByUserId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.familyId = :familyId")
    void revokeTokenFamily(@Param("familyId") String familyId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.familyId = :familyId AND rt.revoked = false")
    List<RefreshToken> findByFamilyIdAndRevokedFalse(@Param("familyId") String familyId);
}