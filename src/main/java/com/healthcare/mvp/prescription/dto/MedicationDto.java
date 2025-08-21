package com.healthcare.mvp.prescription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationDto {
    private String name;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String route; // ORAL, IV, TOPICAL, etc.
}
