package com.healthcare.mvp.doctor.service;

import com.healthcare.mvp.doctor.dto.CreateDoctorRequest;
import com.healthcare.mvp.doctor.dto.DoctorDto;
import com.healthcare.mvp.doctor.entity.Doctor;
import com.healthcare.mvp.doctor.repository.DoctorRepository;
import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    /**
     * Create a new doctor
     */
    public DoctorDto createDoctor(CreateDoctorRequest request) {
        log.info("Creating new doctor for hospital: {}", request.getHospitalId());

        // Validate hospital exists
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found with ID: " + request.getHospitalId()));

        // Validate email uniqueness
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Validate medical license number uniqueness
        if (doctorRepository.existsByMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
            throw new RuntimeException("Medical license number already exists: " + request.getMedicalLicenseNumber());
        }

        // Generate doctor code
        String doctorCode = generateDoctorCode();

        // Create doctor entity
        Doctor doctor = new Doctor();
        doctor.setDoctorId(UUID.randomUUID());
        doctor.setHospitalId(hospital.getHospitalId());
        doctor.setDoctorCode(doctorCode);
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setMedicalLicenseNumber(request.getMedicalLicenseNumber());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setDepartment(request.getDepartment());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setDateOfJoining(request.getDateOfJoining());
        doctor.setBio(request.getBio());
        doctor.setIsTelemedicineEnabled(request.getIsTelemedicineEnabled());
        doctor.setIsActive(true);
        doctor.setCreatedBy(getCurrentUserId());
        doctor.setUpdatedBy(getCurrentUserId());

        // Convert availability to JSON strings (simplified for now)
        if (request.getAvailableDays() != null) {
            doctor.setAvailableDays(String.join(",", request.getAvailableDays()));
        }

        if (request.getAvailableStartTime() != null && request.getAvailableEndTime() != null) {
            doctor.setAvailableHours(String.format("{\"start\":\"%s\",\"end\":\"%s\"}",
                    request.getAvailableStartTime(), request.getAvailableEndTime()));
        }

        if (request.getLanguagesSpoken() != null) {
            doctor.setLanguagesSpoken(String.join(",", request.getLanguagesSpoken()));
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor created successfully with ID: {} and code: {}", savedDoctor.getDoctorId(), savedDoctor.getDoctorCode());

        return convertToDto(savedDoctor);
    }

    /**
     * Get doctors by hospital - FIXED with alternative approach
     */
    public List<DoctorDto> getHospitalDoctors(UUID hospitalId) {
        log.debug("Fetching doctors for hospital: {}", hospitalId);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        // Use the direct field approach as a fallback
        try {
            return doctorRepository.findByHospitalHospitalIdAndIsActive(hospitalId, true)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to direct field method
            log.debug("Using fallback method for hospital doctors query");
            return doctorRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get doctor by ID
     */
    public Optional<DoctorDto> getDoctorById(UUID doctorId) {
        log.debug("Fetching doctor by ID: {}", doctorId);

        return doctorRepository.findById(doctorId)
                .filter(doctor -> Boolean.TRUE.equals(doctor.getIsActive()))
                .map(this::convertToDto);
    }

    /**
     * Get doctor by code - FIXED method call
     */
    public Optional<DoctorDto> getDoctorByCode(String doctorCode) {
        log.debug("Fetching doctor by code: {}", doctorCode);

        return doctorRepository.findByDoctorCode(doctorCode)
                .filter(doctor -> Boolean.TRUE.equals(doctor.getIsActive()))
                .map(this::convertToDto);
    }

    /**
     * Search doctors - FIXED method call
     */
    public List<DoctorDto> searchDoctors(String searchTerm) {
        log.debug("Searching doctors with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new RuntimeException("Search term cannot be empty");
        }

        return doctorRepository.searchByNameOrSpecialization(searchTerm.trim())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get doctors by specialization - FIXED method call
     */
    public List<DoctorDto> getDoctorsBySpecialization(String specialization) {
        log.debug("Fetching doctors by specialization: {}", specialization);

        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .filter(doctor -> Boolean.TRUE.equals(doctor.getIsActive()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update doctor
     */
    public DoctorDto updateDoctor(UUID doctorId, CreateDoctorRequest request) {
        log.info("Updating doctor: {}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new RuntimeException("Cannot update inactive doctor");
        }

        // Validate email uniqueness if changed
        if (!request.getEmail().equals(doctor.getEmail()) && doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Validate license number uniqueness if changed
        if (!request.getMedicalLicenseNumber().equals(doctor.getMedicalLicenseNumber())
            && doctorRepository.existsByMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
            throw new RuntimeException("Medical license number already exists: " + request.getMedicalLicenseNumber());
        }

        // Update doctor fields
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setMedicalLicenseNumber(request.getMedicalLicenseNumber());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setDepartment(request.getDepartment());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBio(request.getBio());
        doctor.setIsTelemedicineEnabled(request.getIsTelemedicineEnabled());
        doctor.setUpdatedBy(getCurrentUserId());

        // Update availability
        if (request.getAvailableDays() != null) {
            doctor.setAvailableDays(String.join(",", request.getAvailableDays()));
        }

        if (request.getAvailableStartTime() != null && request.getAvailableEndTime() != null) {
            doctor.setAvailableHours(String.format("{\"start\":\"%s\",\"end\":\"%s\"}",
                    request.getAvailableStartTime(), request.getAvailableEndTime()));
        }

        if (request.getLanguagesSpoken() != null) {
            doctor.setLanguagesSpoken(String.join(",", request.getLanguagesSpoken()));
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor updated successfully: {}", savedDoctor.getDoctorCode());

        return convertToDto(savedDoctor);
    }

    /**
     * Deactivate doctor
     */
    public void deactivateDoctor(UUID doctorId) {
        log.info("Deactivating doctor: {}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        doctor.setIsActive(false);
        doctor.setUpdatedBy(getCurrentUserId());
        doctorRepository.save(doctor);

        log.info("Doctor deactivated successfully: {}", doctor.getDoctorCode());
    }

    /**
     * Get doctors by department
     */
    public List<DoctorDto> getDoctorsByDepartment(String department) {
        log.debug("Fetching doctors by department: {}", department);

        return doctorRepository.findByDepartmentAndIsActiveTrue(department)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get telemedicine enabled doctors
     */
    public List<DoctorDto> getTelemedicineDoctors(UUID hospitalId) {
        log.debug("Fetching telemedicine doctors for hospital: {}", hospitalId);

        return doctorRepository.findTelemedicineDoctorsByHospital(hospitalId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========================= HELPER METHODS =========================

    /**
     * Generate unique doctor code
     */
    private String generateDoctorCode() {
        Long count = doctorRepository.count();
        int nextNumber = count.intValue() + 1;
        String code;
        do {
            code = "DOC" + String.format("%05d", nextNumber);
            nextNumber++;
        } while (doctorRepository.existsByDoctorCode(code));

        return code;
    }

    /**
     * Get current user ID (placeholder)
     */
    private UUID getCurrentUserId() {
        // TODO: Implement proper user context retrieval from SecurityContext
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    /**
     * Convert Doctor entity to DoctorDto - FIXED and COMPLETE
     */
    private DoctorDto convertToDto(Doctor doctor) {
        DoctorDto dto = new DoctorDto();

        // Basic information
        dto.setDoctorId(doctor.getDoctorId());
        dto.setHospitalId(doctor.getHospitalId());
        dto.setDoctorCode(doctor.getDoctorCode());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setMedicalLicenseNumber(doctor.getMedicalLicenseNumber());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setDepartment(doctor.getDepartment());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setDateOfJoining(doctor.getDateOfJoining());
        dto.setProfilePictureUrl(doctor.getProfilePictureUrl());
        dto.setBio(doctor.getBio());
        dto.setIsTelemedicineEnabled(doctor.getIsTelemedicineEnabled());
        dto.setIsActive(doctor.getIsActive());
        dto.setCreatedAt(doctor.getCreatedAt() != null ? doctor.getCreatedAt() : null);
        dto.setUpdatedAt(doctor.getUpdatedAt() != null ? doctor.getUpdatedAt() : null);

        // Parse availability information (simplified)
        if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().isEmpty()) {
            dto.setAvailableDays(List.of(doctor.getAvailableDays().split(",")));
        }

        // Parse available hours from JSON (simplified)
        if (doctor.getAvailableHours() != null && !doctor.getAvailableHours().isEmpty()) {
            // Simple parsing - in production, use proper JSON parsing
            String hours = doctor.getAvailableHours();
            if (hours.contains("start") && hours.contains("end")) {
                // Extract start and end times (simplified)
                try {
                    String startTime = hours.substring(hours.indexOf("start\":\"") + 8);
                    startTime = startTime.substring(0, startTime.indexOf("\""));
                    dto.setAvailableStartTime(startTime);

                    String endTime = hours.substring(hours.indexOf("end\":\"") + 6);
                    endTime = endTime.substring(0, endTime.indexOf("\""));
                    dto.setAvailableEndTime(endTime);
                } catch (Exception e) {
                    log.warn("Failed to parse available hours for doctor {}: {}", doctor.getDoctorId(), e.getMessage());
                }
            }
        }

        // Parse languages
        if (doctor.getLanguagesSpoken() != null && !doctor.getLanguagesSpoken().isEmpty()) {
            dto.setLanguagesSpoken(List.of(doctor.getLanguagesSpoken().split(",")));
        }

        // Set hospital name if relationship is loaded
//        if (doctor.getHospitalId() != null) {
//            dto.setHospitalName(doctor.getHospitalId().getHospitalName());
//        } else {
            // Fetch hospital name separately
           Hospital hospital= hospitalRepository.findByHospitalId(doctor.getHospitalId())
                    .orElseThrow(() -> new RuntimeException("Hospital not found with ID: " + doctor.getHospitalId()));


        dto.setHospitalName(hospital.getHospitalName());
//        }

        return dto;
    }
}
