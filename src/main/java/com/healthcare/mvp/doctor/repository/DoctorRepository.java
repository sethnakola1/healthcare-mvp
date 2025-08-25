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

    Optional<Doctor> findByDoctorCode(String doctorCode);

    boolean existsByDoctorCode(String doctorCode);

    boolean existsByEmail(String email);

    boolean existsByMedicalLicenseNumber(String medicalLicenseNumber);

    List<Doctor> findByHospitalIdAndIsActiveTrue(UUID hospitalId);

    List<Doctor> findByHospitalHospitalIdAndIsActive(UUID hospitalId, boolean isActive);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Doctor> searchByNameOrSpecialization(@Param("searchTerm") String searchTerm);

    List<Doctor> findByDepartmentAndIsActiveTrue(String department);

    @Query("SELECT d FROM Doctor d WHERE d.isTelemedicineEnabled = true AND d.hospitalId = :hospitalId AND d.isActive = true")
    List<Doctor> findTelemedicineDoctorsByHospital(@Param("hospitalId") UUID hospitalId);

    // Add method for specialization search
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.department) = LOWER(:specialization) AND d.isActive = true")
    List<Doctor> findBySpecialization(@Param("specialization") String specialization);
}