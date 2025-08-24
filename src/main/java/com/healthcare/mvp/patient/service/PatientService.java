package com.healthcare.mvp.patient.service;

import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import com.healthcare.mvp.patient.dto.CreatePatientRequest;
import com.healthcare.mvp.patient.dto.PatientDto;
import com.healthcare.mvp.patient.entity.Patient;
import com.healthcare.mvp.patient.repository.PatientRepository;
import com.healthcare.mvp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    /**
     * Register a new patient
     */
    public PatientDto registerPatient(CreatePatientRequest request) {
        log.info("Registering new patient for hospital: {}", request.getHospitalId());

        // Validate hospital exists
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found with ID: " + request.getHospitalId()));
        
        // Validate email uniqueness if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (patientRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        // Generate unique IDs
        String globalPatientId = generateGlobalPatientId();
        String mrn = generateMrn(hospital.getHospitalId());

        // Ensure MRN is unique within hospital (safety check)
        int attempts = 0;
        while (patientRepository.existsByMrnAndHospitalId(mrn, hospital.getHospitalId()) && attempts < 10) {
            mrn = generateMrn(hospital.getHospitalId());
            attempts++;
        }
        
        if (attempts >= 10) {
            throw new RuntimeException("Failed to generate unique MRN after 10 attempts");
        }

        // Create patient entity
        Patient patient = Patient.builder()
                .patientId(UUID.randomUUID())
                .hospitalId(hospital.getHospitalId())
                .globalPatientId(globalPatientId)
                .mrn(mrn)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .email(request.getEmail())
                .bloodGroup(request.getBloodGroup())
                .contactInfo(buildContactInfo(request))
                .encryptedFirstname(new byte[0]) // Placeholder for encryption
                .encryptedLastname(new byte[0])  // Placeholder for encryption
                .isEncrypted(false) // Set to true when implementing encryption
//                .isActive(true)
//                .createdBy(getCurrentUserId())
//                .updatedBy(getCurrentUserId())
                .build();
        
        // Save patient
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient registered successfully with Global ID: {} and MRN: {}",
                savedPatient.getGlobalPatientId(), savedPatient.getMrn());

        return convertToDto(savedPatient);
    }
    
    /**
     * Get patients by hospital with pagination
     */
    public Page<PatientDto> getPatientsByHospital(UUID hospitalId, Pageable pageable) {
        log.debug("Fetching patients for hospital: {} with pagination", hospitalId);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        return patientRepository.findByHospitalIdAndIsActiveTrue(hospitalId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Search patients by hospital with pagination
     */
    public Page<PatientDto> searchPatientsByHospital(UUID hospitalId, String searchTerm, Pageable pageable) {
        log.debug("Searching patients for hospital: {} with term: {}", hospitalId, searchTerm);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        return patientRepository.searchPatientsByHospitalWithPagination(hospitalId, searchTerm, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get hospital patients (non-paginated for backward compatibility)
     */
    public List<PatientDto> getHospitalPatients(UUID hospitalId) {
        log.debug("Fetching all active patients for hospital: {}", hospitalId);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        return patientRepository.findByHospitalIdAndIsActive(hospitalId, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Search patients globally across all hospitals
     */
    public List<PatientDto> searchPatientsGlobally(String searchTerm) {
        log.debug("Searching patients globally with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new RuntimeException("Search term cannot be empty");
        }

        return patientRepository.searchPatients(searchTerm.trim())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get patient by ID
     */
    public Optional<PatientDto> getPatientById(UUID patientId) {
        log.debug("Fetching patient by ID: {}", patientId);

        return patientRepository.findById(patientId)
                .filter(Patient::getIsActive)
                .map(this::convertToDto);
    }
    
    /**
     * Update patient details
     */
    public PatientDto updatePatient(UUID patientId, CreatePatientRequest request) {
        log.info("Updating patient: {}", patientId);

        // Find existing patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        if (!patient.getIsActive()) {
            throw new RuntimeException("Cannot update inactive patient");
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(patient.getEmail())) {
            if (patientRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        // Update patient fields
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setEmail(request.getEmail());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setContactInfo(buildContactInfo(request));
        patient.setUpdatedBy(getCurrentUserId());

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient updated successfully: {}", savedPatient.getGlobalPatientId());

        return convertToDto(savedPatient);
    }
    
    /**
     * Get patient by global patient ID
     */
    public Optional<PatientDto> getPatientByGlobalId(String globalPatientId) {
        log.debug("Fetching patient by global ID: {}", globalPatientId);

        return patientRepository.findByGlobalPatientId(globalPatientId)
                .filter(Patient::getIsActive)
                .map(this::convertToDto);
    }
    
    /**
     * Get patients by MRN (can exist in multiple hospitals)
     */
    public List<PatientDto> getPatientsByMrn(String mrn) {
        log.debug("Fetching patients by MRN: {}", mrn);

        return patientRepository.findByMrn(mrn)
                .stream()
                .filter(Patient::getIsActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate patient (soft delete)
     */
    public void deactivatePatient(UUID patientId) {
        log.info("Deactivating patient: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        patient.setIsActive(false);
        patient.setUpdatedBy(getCurrentUserId());
        patientRepository.save(patient);

        log.info("Patient deactivated successfully: {}", patient.getGlobalPatientId());
    }

    // ========================= HELPER METHODS =========================

    /**
     * Generate global patient ID (PAT0000001)
     */
    private String generateGlobalPatientId() {
        Long count = patientRepository.countAllActivePatients();
        if (count == null) count = 0L;
        int nextNumber = count.intValue() + 1;
        return "PAT" + String.format("%07d", nextNumber);
    }

    /**
     * Generate MRN for specific hospital (MRN000001)
     */
    private String generateMrn(UUID hospitalId) {
        Long count = patientRepository.countActivePatientsByHospital(hospitalId);
        if (count == null) count = 0L;
        int nextNumber = count.intValue() + 1;
        return "MRN" + String.format("%06d", nextNumber);
    }

    /**
     * Build contact info JSON string
     */
    private String buildContactInfo(CreatePatientRequest request) {
        StringBuilder contactInfo = new StringBuilder("{");
        boolean hasContent = false;

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            contactInfo.append("\"phone\":\"").append(request.getPhoneNumber().trim()).append("\"");
            hasContent = true;
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            if (hasContent) contactInfo.append(",");
            contactInfo.append("\"address\":\"").append(request.getAddress().trim().replace("\"", "\\\"")).append("\"");
            hasContent = true;
        }

        contactInfo.append("}");
        return hasContent ? contactInfo.toString() : "{}";
    }

    /**
     * Get current user ID (placeholder - implement proper user context)
     */
    private UUID getCurrentUserId() {
        // TODO: Implement proper user context retrieval from SecurityContext
        // For now, return a fixed UUID or retrieve from authentication
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    /**
     * Convert Patient entity to PatientDto
     */
    private PatientDto convertToDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(patient.getPatientId());
        dto.setHospitalId(patient.getHospitalId());
        dto.setGlobalPatientId(patient.getGlobalPatientId());
        dto.setMrn(patient.getMrn());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setEmail(patient.getEmail());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setContactInfo(patient.getContactInfo());
        dto.setIsActive(patient.getIsActive());
        dto.setCreatedAt(OffsetDateTime.from(patient.getCreatedDate()));
        dto.setUpdatedAt(OffsetDateTime.from(patient.getUpdatedDate()));

        // Set hospital name if hospital relationship is loaded
        if (patient.getHospital() != null) {
            dto.setHospitalName(patient.getHospital().getHospitalName());
        } else {
            // Fetch hospital name separately
            hospitalRepository.findById(patient.getHospitalId())
                    .ifPresent(hospital -> dto.setHospitalName(hospital.getHospitalName()));
        }

        return dto;
    }
}