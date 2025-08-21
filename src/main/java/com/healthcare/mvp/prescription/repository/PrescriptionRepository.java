package com.healthcare.mvp.prescription.repository;

import com.healthcare.mvp.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    List<Prescription> findByDoctorIdAndIsActive(UUID doctorId, boolean isActive);

    List<Prescription> findByPatientIdAndIsActive(UUID patientId, boolean isActive);
}