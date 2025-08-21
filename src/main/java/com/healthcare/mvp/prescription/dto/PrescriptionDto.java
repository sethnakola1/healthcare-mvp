package com.healthcare.mvp.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionDto {
    private UUID prescriptionId;
    private UUID patientId;
    private UUID doctorId;
    private UUID hospitalId;
    private UUID appointmentId;
    private UUID medicalRecordId;
    private LocalDate prescriptionDate;
    private String prescriptionNumber;
    @JdbcTypeCode(SqlTypes.JSON)
    private String medications;
    private String generalInstructions;
    private String dietaryInstructions;
    private LocalDate followUpDate;
    private Boolean pdfGenerated;
    private String pdfFilePath;
    private LocalDateTime pdfGeneratedAt;
    private String status;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    private String dosage;
    private String fhirResourceId;
    private Boolean isActive;
    private String medication;
}