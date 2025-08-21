package com.healthcare.mvp.appointment.repository;

import com.healthcare.mvp.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * FIXED: Using direct field references instead of relationship navigation
     * This avoids the "No property 'id' found for type 'Hospital'" error
     */

    // Basic finders with pagination
    Page<Appointment> findByHospitalIdAndPatientIdAndIsActiveTrue(UUID hospitalId, UUID patientId, Pageable pageable);

    Page<Appointment> findByHospitalIdAndDoctorIdAndIsActiveTrue(UUID hospitalId, UUID doctorId, Pageable pageable);

    boolean existsByAppointmentIdAndHospitalIdAndIsActiveTrue(UUID appointmentId, UUID hospitalId);

    // Hospital appointments
    List<Appointment> findByHospitalIdAndIsActiveTrue(UUID hospitalId);

    Page<Appointment> findByHospitalIdAndIsActiveTrue(UUID hospitalId, Pageable pageable);

    // Patient appointments
    List<Appointment> findByPatientIdAndIsActiveTrue(UUID patientId);

    Page<Appointment> findByPatientIdAndIsActiveTrue(UUID patientId, Pageable pageable);

    // Doctor appointments
    List<Appointment> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    Page<Appointment> findByDoctorIdAndIsActiveTrue(UUID doctorId, Pageable pageable);

    // Status-based queries
    List<Appointment> findByHospitalIdAndStatusAndIsActiveTrue(UUID hospitalId, Appointment.AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatusAndIsActiveTrue(UUID doctorId, Appointment.AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatusAndIsActiveTrue(UUID patientId, Appointment.AppointmentStatus status);

    // Date range queries
    @Query("SELECT a FROM Appointment a WHERE a.hospitalId = :hospitalId AND a.appointmentDateTime BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Appointment> findByHospitalIdAndDateRange(@Param("hospitalId") UUID hospitalId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDateTime BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Appointment> findByDoctorIdAndDateRange(@Param("doctorId") UUID doctorId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.appointmentDateTime BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Appointment> findByPatientIdAndDateRange(@Param("patientId") UUID patientId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Today's appointments
//    @Query("SELECT a FROM Appointment a WHERE a.hospitalId = :hospitalId AND DATE(a.appointmentDateTime) = CURRENT_DATE AND a.isActive = true ORDER BY a.appointmentDateTime")
//    List<Appointment> findTodaysAppointmentsByHospital(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT a FROM Appointment a WHERE a.hospitalId = :hospitalId " +
            "AND a.appointmentDateTime >= :startOfDay " +
            "AND a.appointmentDateTime < :endOfDay " +
            "AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findTodaysAppointmentsByHospital(@Param("hospitalId") UUID hospitalId,
                                                       @Param("startOfDay") LocalDateTime startOfDay,
                                                       @Param("endOfDay") LocalDateTime endOfDay);

//    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND DATE(a.appointmentDateTime) = CURRENT_DATE AND a.isActive = true ORDER BY a.appointmentDateTime")
//    List<Appointment> findTodaysAppointmentsByDoctor(@Param("doctorId") UUID doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
            "AND a.appointmentDateTime >= :startOfDay " +
            "AND a.appointmentDateTime < :endOfDay " +
            "AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findTodaysAppointmentsByDoctor(@Param("doctorId") UUID doctorId,
                                                     @Param("startOfDay") LocalDateTime startOfDay,
                                                     @Param("endOfDay") LocalDateTime endOfDay);

//    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND DATE(a.appointmentDateTime) = CURRENT_DATE AND a.isActive = true ORDER BY a.appointmentDateTime")
//    List<Appointment> findTodaysAppointmentsByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
            "AND a.appointmentDateTime >= :startOfDay " +
            "AND a.appointmentDateTime < :endOfDay " +
            "AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findTodaysAppointmentsByPatient(@Param("patientId") UUID patientId,
                                                      @Param("startOfDay") LocalDateTime startOfDay,
                                                      @Param("endOfDay") LocalDateTime endOfDay);

    // Upcoming appointments (future)
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDateTime > :currentTime AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findUpcomingAppointmentsByDoctor(@Param("doctorId") UUID doctorId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.appointmentDateTime > :currentTime AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") UUID patientId, @Param("currentTime") LocalDateTime currentTime);

    // Count queries for statistics
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.hospitalId = :hospitalId AND a.isActive = true")
    Long countActiveAppointmentsByHospital(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND a.isActive = true")
    Long countActiveAppointmentsByDoctor(@Param("doctorId") UUID doctorId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patientId = :patientId AND a.isActive = true")
    Long countActiveAppointmentsByPatient(@Param("patientId") UUID patientId);

    // TODO: Uncomment when needed - Conflict detection (same doctor, overlapping time)
    // @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDateTime = :appointmentDateTime AND a.isActive = true AND (:appointmentId IS NULL OR a.appointmentId != :appointmentId)")
    // boolean existsConflictingAppointment(@Param("doctorId") UUID doctorId,
    //                                      @Param("appointmentDateTime") LocalDateTime appointmentDateTime,
    //                                      @Param("appointmentId") UUID appointmentId);

    // Find appointments by appointment type
    List<Appointment> findByHospitalIdAndAppointmentTypeAndIsActiveTrue(UUID hospitalId, Appointment.AppointmentType appointmentType);

    // Virtual appointments
    @Query("SELECT a FROM Appointment a WHERE a.hospitalId = :hospitalId AND a.isVirtual = true AND a.isActive = true")
    List<Appointment> findVirtualAppointmentsByHospital(@Param("hospitalId") UUID hospitalId);

    // Emergency appointments
    @Query("SELECT a FROM Appointment a WHERE a.hospitalId = :hospitalId AND a.isEmergency = true AND a.isActive = true ORDER BY a.appointmentDateTime")
    List<Appointment> findEmergencyAppointmentsByHospital(@Param("hospitalId") UUID hospitalId);
}
