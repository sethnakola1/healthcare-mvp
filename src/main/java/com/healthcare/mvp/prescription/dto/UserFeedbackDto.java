package com.healthcare.mvp.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFeedbackDto {
    private UUID feedbackId;
    private UUID patientId;
    private UUID hospitalId;
    private UUID appointmentId;
    private String feedbackType;
    private UUID targetUserId;
    private Integer overallRating;
    private Integer serviceQualityRating;
    private Integer waitTimeRating;
    private Integer staffBehaviorRating;
    private Integer facilityCleanlinessRating;
    private String comments;
    private String suggestions;
    private String complaints;
    private Boolean isAnonymous;
    private String feedbackStatus;
    private String hospitalResponse;
    private UUID respondedBy;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
}