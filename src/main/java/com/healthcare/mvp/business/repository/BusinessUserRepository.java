package com.healthcare.mvp.business.repository;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.shared.constants.BusinessRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessUserRepository extends JpaRepository<BusinessUser, UUID> {

    /**
     * Find user by email
     */
    Optional<BusinessUser> findByEmail(String email);

    /**
     * Find user by username
     */
    Optional<BusinessUser> findByUsername(String username);

    /**
     * Find user by partner code
     */
    Optional<BusinessUser> findByPartnerCode(String partnerCode);

    /**
     * Find users by business role
     */
    List<BusinessUser> findByBusinessRole(BusinessRole businessRole);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if partner code exists
     */
    boolean existsByPartnerCode(String partnerCode);

    /**
     * Find active users by role
     */
    @Query("SELECT bu FROM BusinessUser bu WHERE bu.businessRole = :role AND bu.isActive = true")
    List<BusinessUser> findActiveUsersByRole(@Param("role") BusinessRole role);

    /**
     * Find active tech advisors
     */
    @Query("SELECT bu FROM BusinessUser bu WHERE bu.businessRole = 'TECH_ADVISOR' AND bu.isActive = true ORDER BY bu.createdAt DESC")
    List<BusinessUser> findActiveTechAdvisors();

    /**
     * Find users by territory
     */
    @Query("SELECT bu FROM BusinessUser bu WHERE bu.territory = :territory AND bu.isActive = true")
    List<BusinessUser> findActiveUsersByTerritory(@Param("territory") String territory);

    /**
     * Find top performing tech advisors
     */
    @Query("SELECT bu FROM BusinessUser bu WHERE bu.businessRole = 'TECH_ADVISOR' AND bu.isActive = true ORDER BY bu.totalCommissionEarned DESC, bu.totalHospitalsBrought DESC")
    List<BusinessUser> findTopPerformingTechAdvisors();

    /**
     * Count active users by role
     */
    @Query("SELECT COUNT(bu) FROM BusinessUser bu WHERE bu.businessRole = :role AND bu.isActive = true")
    long countActiveUsersByRole(@Param("role") BusinessRole role);

    /**
     * Finds a BusinessUser by their reset token.
     *
     * @param resetToken The reset token associated with the user.
     * @return An Optional containing the BusinessUser if found, or empty if not found.
     */
    Optional<BusinessUser> findByResetToken(String resetToken);


    @Modifying
    @Query(value = "UPDATE business_user SET password_hash = :passwordHash, login_attempts = 0, account_locked_until = NULL WHERE email = :email", nativeQuery = true)
    int updatePasswordHash(@Param("email") String email, @Param("passwordHash") String passwordHash);
}