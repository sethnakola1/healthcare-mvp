package com.healthcare.mvp.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.mvp.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogger {

    private final ObjectMapper objectMapper;
    private static final org.slf4j.Logger auditLogger = org.slf4j.LoggerFactory.getLogger("com.healthcare.mvp.audit");

    public void logDataAccess(String entityType, String entityId, String action) {
        logDataAccess(entityType, entityId, action, null);
    }

    public void logDataAccess(String entityType, String entityId, String action, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventType("DATA_ACCESS")
                .userId(SecurityUtils.getCurrentUserId())
                .userEmail(SecurityUtils.getCurrentUserEmail())
//                .hospitalId(SecurityUtils.getCurrentHospitalId())
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .sensitiveDataAccessed(isSensitiveEntity(entityType))
                .patientDataAccessed(isPatientData(entityType))
                .build();

        logAuditEvent(event);
    }

    public void logAuthenticationEvent(String userId, String email, String action, boolean success) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("success", success);
        metadata.put("action", action);

        AuditEvent event = AuditEvent.builder()
                .eventType("AUTHENTICATION")
                .userId(userId)
                .userEmail(email)
                .action(action)
                .details(success ? "Authentication successful" : "Authentication failed")
                .metadata(metadata)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

        logAuditEvent(event);
    }

    public void logBusinessEvent(String eventType, String description) {
        logBusinessEvent(eventType, description, null);
    }

    public void logBusinessEvent(String eventType, String description, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventType(eventType)
                .userId(SecurityUtils.getCurrentUserId())
                .userEmail(SecurityUtils.getCurrentUserEmail())
//                .hospitalId(SecurityUtils.getCurrentHospitalId())
                .action(eventType)
                .details(description)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

        logAuditEvent(event);
    }

    public void logSecurityEvent(String eventType, String description, String severity) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("severity", severity);

        AuditEvent event = AuditEvent.builder()
                .eventType("SECURITY_EVENT")
                .userId(SecurityUtils.getCurrentUserId())
                .userEmail(SecurityUtils.getCurrentUserEmail())
                .action(eventType)
                .details(description)
                .metadata(metadata)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

        logAuditEvent(event);
    }

    private void logAuditEvent(AuditEvent event) {
        try {
            MDC.put("eventType", event.getEventType());
            MDC.put("userId", event.getUserId());
            MDC.put("action", event.getAction());
            MDC.put("entityType", event.getEntityType());
            MDC.put("timestamp", event.getTimestamp().toString());

            auditLogger.info("AUDIT_EVENT: {}", objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        } finally {
            MDC.clear();
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "unknown";
        }
    }

    private boolean isSensitiveEntity(String entityType) {
        return entityType != null && (
            entityType.equalsIgnoreCase("PATIENT") ||
            entityType.equalsIgnoreCase("MEDICAL_RECORD") ||
            entityType.equalsIgnoreCase("PRESCRIPTION") ||
            entityType.equalsIgnoreCase("BILLING")
        );
    }

    private boolean isPatientData(String entityType) {
        return entityType != null && (
            entityType.equalsIgnoreCase("PATIENT") ||
            entityType.equalsIgnoreCase("MEDICAL_RECORD") ||
            entityType.equalsIgnoreCase("APPOINTMENT") ||
            entityType.equalsIgnoreCase("PRESCRIPTION") ||
            entityType.equalsIgnoreCase("BILLING")
        );
    }
}