package com.healthcare.mvp.prescription.service;

import com.healthcare.mvp.prescription.dto.CreatePrescriptionRequest;
import com.healthcare.mvp.prescription.dto.PrescriptionDto;
import com.healthcare.mvp.prescription.entity.Prescription;
import com.healthcare.mvp.prescription.repository.PrescriptionRepository;
import com.healthcare.mvp.shared.exception.AuthorizationException;
import com.healthcare.mvp.shared.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    /**
     * Get prescriptions issued by a specific doctor
     */
    public List<PrescriptionDto> getDoctorPrescriptions(UUID doctorId) {
        log.info("Fetching prescriptions for doctor ID: {}", doctorId);
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentAuthentication().getAuthorities().iterator().next().getAuthority();

        // Restrict access to doctors or super admins
        if (!currentUserRole.contains("DOCTOR") && !currentUserRole.contains("SUPER_ADMIN")) {
            log.warn("Unauthorized access attempt to doctor prescriptions by user: {}", currentUserId);
            throw new AuthorizationException("Only doctors or super admins can access doctor prescriptions");
        }

        // If not super admin, ensure the doctor is accessing their own prescriptions
        if (!currentUserRole.contains("SUPER_ADMIN") && !currentUserId.equals(doctorId.toString())) {
            log.warn("Doctor {} attempted to access another doctor's prescriptions", currentUserId);
            throw new AuthorizationException("Doctors can only access their own prescriptions");
        }

        List<PrescriptionDto> prescriptions = prescriptionRepository.findByDoctorIdAndIsActive(doctorId, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} prescriptions for doctor ID: {}", prescriptions.size(), doctorId);
        return prescriptions;
    }

    /**
     * Get prescriptions for a specific patient
     */
    public List<PrescriptionDto> getPatientPrescriptions(UUID patientId) {
        log.info("Fetching prescriptions for patient ID: {}", patientId);
        String currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentAuthentication().getAuthorities().iterator().next().getAuthority();

        // Restrict access to patients, doctors, or super admins
        if (!currentUserRole.contains("PATIENT") && !currentUserRole.contains("DOCTOR") && !currentUserRole.contains("SUPER_ADMIN")) {
            log.warn("Unauthorized access attempt to patient prescriptions by user: {}", currentUserId);
            throw new AuthorizationException("Only patients, doctors, or super admins can access patient prescriptions");
        }

        // If patient, ensure they are accessing their own prescriptions
        if (currentUserRole.contains("PATIENT") && !currentUserId.equals(patientId.toString())) {
            log.warn("Patient {} attempted to access another patient's prescriptions", currentUserId);
            throw new AuthorizationException("Patients can only access their own prescriptions");
        }

        List<PrescriptionDto> prescriptions = prescriptionRepository.findByPatientIdAndIsActive(patientId, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} prescriptions for patient ID: {}", prescriptions.size(), patientId);
        return prescriptions;
    }

    /**
     * Convert entity to DTO
     */
    private PrescriptionDto convertToDto(Prescription prescription) {
        return PrescriptionDto.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .patientId(prescription.getPatientId())
                .doctorId(prescription.getDoctorId())
                .hospitalId(prescription.getHospitalId())
                .appointmentId(prescription.getAppointmentId())
                .medicalRecordId(prescription.getMedicalRecordId())
                .prescriptionDate(prescription.getPrescriptionDate())
                .prescriptionNumber(prescription.getPrescriptionNumber())
                .medications(prescription.getMedications())
                .generalInstructions(prescription.getGeneralInstructions())
                .dietaryInstructions(prescription.getDietaryInstructions())
                .followUpDate(prescription.getFollowUpDate())
                .pdfGenerated(prescription.getPdfGenerated())
                .pdfFilePath(prescription.getPdfFilePath())
                .pdfGeneratedAt(prescription.getPdfGeneratedAt())
                .status(prescription.getStatus())
                .createdAt(prescription.getCreatedAt())
                .createdBy(prescription.getCreatedBy())
                .updatedAt(prescription.getUpdatedAt())
                .updatedBy(prescription.getUpdatedBy())
                .dosage(prescription.getDosage())
                .fhirResourceId(prescription.getFhirResourceId())
                .isActive(prescription.getIsActive())
                .medication(prescription.getMedication())
                .build();
    }

    public PrescriptionDto createPrescription(@Valid CreatePrescriptionRequest request) {
        return new PrescriptionDto();
    }

    public byte[] generatePrescriptionPdf(UUID prescriptionId) {
        return new byte[0]; // Placeholder for PDF generation logic
    }
}