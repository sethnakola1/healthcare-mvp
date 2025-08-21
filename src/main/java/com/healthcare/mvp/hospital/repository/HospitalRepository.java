package com.healthcare.mvp.hospital.repository;

import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {
    
    Optional<Hospital> findByHospitalCode(String hospitalCode);

    Optional<Hospital> findByHospitalId(UUID hospitalId);

    long countByIsActive(boolean isActive);

    @Query("SELECT COUNT(h) FROM Hospital h")
    long count();
    
    List<Hospital> findByIsActive(Boolean isActive);
    
    /**
     * FIXED: broughtByBusinessUser is a UUID field, not an entity relationship
     * Changed from h.broughtByBusinessUser.businessUserId to h.broughtByBusinessUser
     */
    @Query("SELECT h FROM Hospital h WHERE h.broughtByBusinessUser = :businessUserId AND h.isActive = true")
    List<Hospital> findByBroughtByBusinessUser(@Param("businessUserId") UUID businessUserId);
    
    /**
     * FIXED: techSupport1 and techSupport2 are UUID fields, not entity relationships
     * Changed from h.techSupport1.businessUserId to h.techSupport1
     */
    @Query("SELECT h FROM Hospital h WHERE h.techSupport1 = :techSupportId OR h.techSupport2 = :techSupportId")
    List<Hospital> findByTechSupport(@Param("techSupportId") UUID techSupportId);
    
    @Query("SELECT h FROM Hospital h WHERE h.city = :city AND h.isActive = true")
    List<Hospital> findByCity(@Param("city") String city);
    
    @Query("SELECT h FROM Hospital h WHERE h.state = :state AND h.isActive = true")
    List<Hospital> findByState(@Param("state") String state);
    
    boolean existsByHospitalCode(String hospitalCode);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(h) FROM Hospital h WHERE h.isActive = true")
    Long countActiveHospitals();

    List<HospitalDto> findByPartnerCodeUsed(String partnerCodeUsed);
}