package com.healthcare.mvp.doctor.repository;

import com.healthcare.mvp.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    
    // Basic finders
    Optional<Doctor> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    // Find by hospital ID
    List<Doctor> findByHospitalIdAndIsActiveTrue(UUID hospitalId);

    // For compatibility with service method names
    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.isActive = :isActive")
    List<Doctor> findByHospitalHospitalIdAndIsActive(@Param("hospitalId") UUID hospitalId, @Param("isActive") boolean isActive);

    // Find by doctor code
    Optional<Doctor> findByDoctorCodeAndIsActiveTrue(String doctorCode);

    Optional<Doctor> findByDoctorCode(String doctorCode);

    // Find by specialization
    List<Doctor> findBySpecializationAndIsActiveTrue(String specialization);

    List<Doctor> findBySpecialization(String specialization);

    // Search by name or specialization
    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND " +
           "(LOWER(d.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.department) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Doctor> searchByNameOrSpecialization(@Param("searchTerm") String searchTerm);

    // Additional useful methods
    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.specialization = :specialization AND d.isActive = true")
    List<Doctor> findByHospitalAndSpecialization(@Param("hospitalId") UUID hospitalId, @Param("specialization") String specialization);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.isActive = true")
    Long countActiveDoctorsByHospital(@Param("hospitalId") UUID hospitalId);

    // Existence checks
    boolean existsByDoctorIdAndIsActiveTrue(UUID doctorId);
    boolean existsByEmail(String email);
    boolean existsByMedicalLicenseNumber(String medicalLicenseNumber);
    boolean existsByDoctorCode(String doctorCode);

    // Find by unique fields
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByMedicalLicenseNumber(String medicalLicenseNumber);

    // Find by department
    List<Doctor> findByDepartmentAndIsActiveTrue(String department);

    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.department = :department AND d.isActive = true")
    List<Doctor> findByHospitalAndDepartment(@Param("hospitalId") UUID hospitalId, @Param("department") String department);

    // Find telemedicine enabled doctors
    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.isTelemedicineEnabled = true AND d.isActive = true")
    List<Doctor> findTelemedicineDoctorsByHospital(@Param("hospitalId") UUID hospitalId);

    // Find by experience range
    @Query("SELECT d FROM Doctor d WHERE d.experienceYears BETWEEN :minYears AND :maxYears AND d.isActive = true")
    List<Doctor> findByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Find available doctors
    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.isActive = true AND d.availableDays IS NOT NULL")
    List<Doctor> findAvailableDoctorsByHospital(@Param("hospitalId") UUID hospitalId);

    // Find by consultation fee range
    @Query("SELECT d FROM Doctor d WHERE d.hospitalId = :hospitalId AND d.consultationFee BETWEEN :minFee AND :maxFee AND d.isActive = true")
    List<Doctor> findByHospitalAndConsultationFeeRange(@Param("hospitalId") UUID hospitalId,
                                                       @Param("minFee") java.math.BigDecimal minFee,
                                                       @Param("maxFee") java.math.BigDecimal maxFee);
}