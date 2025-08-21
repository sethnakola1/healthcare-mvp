package com.healthcare.mvp.appointment.service;

import com.healthcare.mvp.appointment.dto.AppointmentDto;
import com.healthcare.mvp.appointment.dto.CreateAppointmentRequest;
import com.healthcare.mvp.appointment.entity.Appointment;
import com.healthcare.mvp.appointment.repository.AppointmentRepository;
import com.healthcare.mvp.doctor.entity.Doctor;
import com.healthcare.mvp.doctor.repository.DoctorRepository;
import com.healthcare.mvp.hospital.entity.Hospital;
import com.healthcare.mvp.hospital.repository.HospitalRepository;
import com.healthcare.mvp.patient.entity.Patient;
import com.healthcare.mvp.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    // TODO: Add DoctorRepository when it's available
    // private final DoctorRepository doctorRepository;

    /**
     * Book a new appointment
     */
    public AppointmentDto bookAppointment(CreateAppointmentRequest request) {
        log.info("Booking appointment for patient: {} with doctor: {}", request.getPatientId(), request.getDoctorId());

        // Validate hospital exists
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found with ID: " + request.getHospitalId()));

        // Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));

        // TODO: Uncomment when DoctorRepository is available
        // Validate doctor exists
        // Doctor doctor = doctorRepository.findById(request.getDoctorId())
        //         .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getDoctorId()));

        // TODO: Uncomment when conflict detection query is implemented
        // Check for conflicting appointments
        // boolean hasConflict = appointmentRepository.existsConflictingAppointment(
        //         request.getDoctorId(),
        //         request.getAppointmentDateTime(),
        //         null
        // );
        //
        // if (hasConflict) {
        //     throw new RuntimeException("Doctor is not available at the requested time");
        // }

        // Validate appointment time is in the future
        if (request.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Appointment cannot be scheduled in the past");
        }
        
        // Create appointment entity
        Appointment appointment = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .hospitalId(hospital.getHospitalId())
                .patientId(patient.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDateTime(request.getAppointmentDateTime())
                .durationMinutes(request.getDurationMinutes())
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .appointmentType(Appointment.AppointmentType.valueOf(request.getAppointmentType()))
                .chiefComplaint(request.getChiefComplaint())
                .notes(request.getNotes())
                .isVirtual(request.getIsVirtual())
                .meetingLink(request.getMeetingLink())
                .isEmergency(request.getIsEmergency())
                .followUpRequired(request.getFollowUpRequired())
                .followUpDate(request.getFollowUpDate())
//                .isActive(true)
//                .createdBy(getCurrentUserId())
//                .updatedBy(getCurrentUserId())
                .build();
        
        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment booked successfully with ID: {}", savedAppointment.getAppointmentId());
        
        return convertToDto(savedAppointment);
    }
    
    /**
     * Get all appointments for a hospital
     */
    public List<AppointmentDto> getHospitalAppointments(UUID hospitalId) {
        log.debug("Fetching appointments for hospital: {}", hospitalId);
        
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }
        
        return appointmentRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get appointments for a specific doctor
     */
    public List<AppointmentDto> getDoctorAppointments(UUID doctorId) {
        log.debug("Fetching appointments for doctor: {}", doctorId);
        
        return appointmentRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get appointments for a specific patient
     */
    public List<AppointmentDto> getPatientAppointments(UUID patientId) {
        log.debug("Fetching appointments for patient: {}", patientId);
        
        // Validate patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found with ID: " + patientId);
        }
        
        return appointmentRepository.findByPatientIdAndIsActiveTrue(patientId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update appointment status
     */
    public AppointmentDto updateAppointmentStatus(UUID appointmentId, String status, String reason) {
        log.info("Updating appointment {} status to: {}", appointmentId, status);

        // Find appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        if (!appointment.getIsActive()) {
            throw new RuntimeException("Cannot update inactive appointment");
        }

        // Validate status
        Appointment.AppointmentStatus appointmentStatus;
        try {
            appointmentStatus = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid appointment status: " + status);
        }

        // Update appointment
        appointment.setStatus(appointmentStatus);
        appointment.setUpdatedBy(getCurrentUserId());

        // Set cancellation reason if cancelled
        if (appointmentStatus == Appointment.AppointmentStatus.CANCELLED && reason != null) {
            appointment.setCancellationReason(reason);
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully: {}", savedAppointment.getAppointmentId());

        return convertToDto(savedAppointment);
    }
    
    /**
     * Cancel an appointment
     */
    public void cancelAppointment(UUID appointmentId, String reason) {
        log.info("Cancelling appointment: {} with reason: {}", appointmentId, reason);

        // Find appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        if (!appointment.getIsActive()) {
            throw new RuntimeException("Cannot cancel inactive appointment");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed appointment");
        }

        // Update appointment status to cancelled
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setUpdatedBy(getCurrentUserId());

        appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully: {}", appointment.getAppointmentId());
    }

    /**
     * Get appointment by ID
     */
    public Optional<AppointmentDto> getAppointmentById(UUID appointmentId) {
        log.debug("Fetching appointment by ID: {}", appointmentId);

        return appointmentRepository.findById(appointmentId)
                .filter(Appointment::getIsActive)
                .map(this::convertToDto);
    }

    /**
     * Get appointments by date range for a hospital
     */
    public List<AppointmentDto> getAppointmentsByDateRange(UUID hospitalId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching appointments for hospital: {} between {} and {}", hospitalId, startDate, endDate);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        return appointmentRepository.findByHospitalIdAndDateRange(hospitalId, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get today's appointments for a hospital
     */
    public List<AppointmentDto> getTodaysAppointments(UUID hospitalId) {
        log.debug("Fetching today's appointments for hospital: {}", hospitalId);

        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return appointmentRepository.findTodaysAppointmentsByHospital(hospitalId, startOfDay, endOfDay)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


//    public List<AppointmentDto> getTodaysAppointments(UUID hospitalId) {
//        log.debug("Fetching today's appointments for hospital: {}", hospitalId);
//
//        return appointmentRepository.findTodaysAppointmentsByHospital(hospitalId)
//                .stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

    /**
     * Get today's appointments for a doctor
     */
    public List<AppointmentDto> getTodaysAppointmentsForDoctor(UUID doctorId) {
        log.debug("Fetching today's appointments for doctor: {}", doctorId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return appointmentRepository.findTodaysAppointmentsByDoctor(doctorId, startOfDay, endOfDay)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

//    public List<AppointmentDto> getTodaysAppointmentsForDoctor(UUID doctorId) {
//        log.debug("Fetching today's appointments for doctor: {}", doctorId);
//
//        return appointmentRepository.findTodaysAppointmentsByDoctor(doctorId)
//                .stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

    /**
     * Get upcoming appointments for a doctor
     */
    public List<AppointmentDto> getUpcomingAppointmentsForDoctor(UUID doctorId) {
        log.debug("Fetching upcoming appointments for doctor: {}", doctorId);

        return appointmentRepository.findUpcomingAppointmentsByDoctor(doctorId, LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming appointments for a patient
     */
    public List<AppointmentDto> getUpcomingAppointmentsForPatient(UUID patientId) {
        log.debug("Fetching upcoming appointments for patient: {}", patientId);

        return appointmentRepository.findUpcomingAppointmentsByPatient(patientId, LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // ========================= HELPER METHODS =========================

    /**
     * Get current user ID (placeholder - implement proper user context)
     */
    private UUID getCurrentUserId() {
        // TODO: Implement proper user context retrieval from SecurityContext
        // For now, return a fixed UUID
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    /**
     * Convert Appointment entity to AppointmentDto
     */
    private AppointmentDto convertToDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();

        // Basic appointment information
        dto.setAppointmentId(appointment.getAppointmentId());
        dto.setHospitalId(appointment.getHospitalId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setAppointmentDateTime(appointment.getAppointmentDateTime());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setStatus(appointment.getStatus().name());
        dto.setAppointmentType(appointment.getAppointmentType().name());
        dto.setChiefComplaint(appointment.getChiefComplaint());
        dto.setNotes(appointment.getNotes());
        dto.setCancellationReason(appointment.getCancellationReason());

        // Virtual appointment details
        dto.setIsVirtual(appointment.getIsVirtual());
        dto.setMeetingLink(appointment.getMeetingLink());

        // Special flags
        dto.setIsEmergency(appointment.getIsEmergency());
        dto.setFollowUpRequired(appointment.getFollowUpRequired());
        dto.setFollowUpDate(appointment.getFollowUpDate());

        // Status and audit
        dto.setIsActive(appointment.getIsActive());
        dto.setCreatedBy(appointment.getCreatedBy());
        dto.setUpdatedBy(appointment.getUpdatedBy());
        dto.setCreatedAt(appointment.getCreatedDate() != null ? appointment.getCreatedDate(): null);
        dto.setUpdatedAt(appointment.getUpdatedDate() != null ? appointment.getUpdatedDate() : null);
        dto.setCreatedDate(OffsetDateTime.from(appointment.getCreatedDate()));
        dto.setUpdatedDate(OffsetDateTime.from(appointment.getUpdatedDate()));
        dto.setVersion(appointment.getVersion());

        // Set names from relationships if loaded
        if (appointment.getHospital() != null) {
            dto.setHospitalName(appointment.getHospital().getHospitalName());
        } else {
            // Fetch hospital name separately
            hospitalRepository.findById(appointment.getHospitalId())
                    .ifPresent(hospital -> dto.setHospitalName(hospital.getHospitalName()));
        }

        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
            dto.setPatientMrn(appointment.getPatient().getMrn());
        } else {
            // Fetch patient name separately
            patientRepository.findById(appointment.getPatientId())
                    .ifPresent(patient -> {
                        dto.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                        dto.setPatientMrn(patient.getMrn());
                    });
        }

        // For doctor information, you'll need to implement DoctorRepository
        // if (appointment.getDoctor() != null) {
        //     dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        //     dto.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
        // } else {
        //     // Fetch doctor name from User or Doctor entity
        //     doctorRepository.findById(appointment.getDoctorId())
        //             .ifPresent(doctor -> {
        //                 dto.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
        //                 dto.setDoctorSpecialization(doctor.getSpecialization());
        //             });
        // }

        // For now, set placeholder doctor info
        dto.setDoctorName("Dr. Sample");
        dto.setDoctorSpecialization("General Medicine");
        
        return dto;
    }
    
    /**
     * Validate appointment time
     */
    private void validateAppointmentTime(LocalDateTime appointmentDateTime) {
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Appointment cannot be scheduled in the past");
        }
        
        // Check if appointment is during business hours (optional)
        int hour = appointmentDateTime.getHour();
        if (hour < 8 || hour > 18) {
            throw new RuntimeException("Appointments can only be scheduled between 8:00 AM and 6:00 PM");
        }
        
        // Check if appointment is on weekend (optional)
        if (appointmentDateTime.getDayOfWeek().getValue() > 5) {
            throw new RuntimeException("Appointments cannot be scheduled on weekends");
        }
    }

    public List<AppointmentDto> getTodaysAppointmentsForPatient(UUID patientId) {
        log.debug("Fetching today's appointments for patient: {}", patientId);

        // Calculate start and end of today
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS); // Midnight of today
        LocalDateTime endOfDay = startOfDay.plusDays(1); // Midnight of tomorrow

        return appointmentRepository.findTodaysAppointmentsByPatient(patientId, startOfDay, endOfDay)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
