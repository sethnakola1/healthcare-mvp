package com.healthcare.mvp.prescription.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CreatePrescriptionRequest {
    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    @NotEmpty(message = "Medications are required")
    private List<MedicationDto> medications;
    @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
    private String instructions;
    @NotNull(message = "Issue date is required")
    private LocalDateTime issueDate;
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;
}