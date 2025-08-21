package com.healthcare.mvp.patient.repository;

import com.healthcare.mvp.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * FIXED: Find active patients by hospital with pagination
     * Using direct hospitalId field instead of hospital.hospitalId
     */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true")
    Page<Patient> findByHospitalIdAndIsActiveTrue(@Param("hospitalId") UUID hospitalId, Pageable pageable);

    /**
     * Find active patients by hospital (non-paginated)
     */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = :isActive")
    List<Patient> findByHospitalIdAndIsActive(@Param("hospitalId") UUID hospitalId, @Param("isActive") Boolean isActive);

    /**
     * FIXED: Check if MRN exists within specific hospital using direct hospitalId field
     */
    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.mrn = :mrn AND p.hospitalId = :hospitalId")
    boolean existsByMrnAndHospitalId(@Param("mrn") String mrn, @Param("hospitalId") UUID hospitalId);

    /**
     * Check if patient exists and is active in hospital
     */
    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.patientId = :patientId AND p.hospitalId = :hospitalId AND p.isActive = true")
    boolean existsByPatientIdAndHospitalIdAndIsActiveTrue(@Param("patientId") UUID patientId, @Param("hospitalId") UUID hospitalId);
    
    /**
     * Find by global patient ID
     */
    Optional<Patient> findByGlobalPatientId(String globalPatientId);
    
    /**
     * Find by MRN (can exist in multiple hospitals)
     */
    List<Patient> findByMrn(String mrn);
    
    /**
     * Email operations
     */
    boolean existsByEmail(String email);
    Optional<Patient> findByEmail(String email);

    /**
     * FIXED: Count active patients by hospital using direct hospitalId field
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true")
    Long countActivePatientsByHospital(@Param("hospitalId") UUID hospitalId);

    /**
     * Count all active patients globally
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isActive = true")
    Long countAllActivePatients();

    /**
     * Global search across all hospitals (non-paginated)
     */
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "p.mrn LIKE CONCAT('%', :searchTerm, '%') OR " +
            "p.globalPatientId LIKE CONCAT('%', :searchTerm, '%'))")
    List<Patient> searchPatients(@Param("searchTerm") String searchTerm);

    /**
     * Search within specific hospital (non-paginated)
     * FIXED: Using direct hospitalId field
     */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "p.mrn LIKE CONCAT('%', :searchTerm, '%'))")
    List<Patient> searchPatientsByHospital(@Param("hospitalId") UUID hospitalId, @Param("searchTerm") String searchTerm);

    /**
     * CRITICAL: Search within specific hospital WITH PAGINATION
     * This method is required by PatientService.searchPatientsByHospital()
     */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "p.mrn LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Patient> searchPatientsByHospitalWithPagination(@Param("hospitalId") UUID hospitalId,
                                                        @Param("searchTerm") String searchTerm,
                                                        Pageable pageable);

    /**
     * Additional useful methods
     */
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    boolean existsByMrn(String mrn);
    
    boolean existsByGlobalPatientId(String globalPatientId);

    List<Patient> findByBloodGroupAndIsActive(String bloodGroup, Boolean isActive);
    
    /**
     * Find patients by age range
     */
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
    List<Patient> findPatientsByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    /**
     * Additional hospital-specific queries using direct hospitalId field
     */
    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true")
    List<Patient> findActivePatientsByHospital(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT p FROM Patient p WHERE p.hospitalId = :hospitalId AND p.isActive = true")
    Page<Patient> findActivePatientsByHospitalWithPagination(@Param("hospitalId") UUID hospitalId, Pageable pageable);
}