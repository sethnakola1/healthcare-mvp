package com.healthcare.mvp.patient.repository;

import com.healthcare.mvp.patient.entity.Encounter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EncounterRepository extends JpaRepository<Encounter, UUID> {
    Page<Encounter> findByHospitalIdAndPatientIdAndIsActiveTrue(UUID hospitalId, UUID patientId, Pageable pageable);

    boolean existsByEncounterIdAndHospitalIdAndIsActiveTrue(UUID encounterId, UUID hospitalId);
}