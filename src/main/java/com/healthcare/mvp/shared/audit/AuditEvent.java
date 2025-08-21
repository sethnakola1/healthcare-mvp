package com.healthcare.mvp.shared.audit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AuditEvent {
    private String eventType;
    private String userId;
    private String userEmail;
    private String hospitalId;
    private String entityType;
    private String entityId;
    private String action;
    private String details;
    private Map<String, Object> metadata;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private boolean sensitiveDataAccessed;
    private boolean patientDataAccessed;
}
