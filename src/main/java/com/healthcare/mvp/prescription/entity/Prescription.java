package com.healthcare.mvp.prescription.entity;

import com.healthcare.mvp.shared.entity.LegacyBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Prescription extends LegacyBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "prescription_id")
    private UUID prescriptionId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "medical_record_id")
    private UUID medicalRecordId;

    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;

    @Column(name = "prescription_number", nullable = false, unique = true)
    private String prescriptionNumber;

    @Column(name = "medications", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String medications;

    @Column(name = "general_instructions")
    private String generalInstructions;

    @Column(name = "dietary_instructions")
    private String dietaryInstructions;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "pdf_generated", nullable = false)
    private Boolean pdfGenerated;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "fhir_resource_id")
    private String fhirResourceId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "medication", nullable = false)
    private String medication;
}