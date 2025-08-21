package com.healthcare.mvp.shared.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogger auditLogger;

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void logDataAccess(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        String entityType = auditLog.entityType();
        String action = auditLog.action();
        
        String entityId = extractEntityId(joinPoint.getArgs(), result);
        
        auditLogger.logDataAccess(entityType, entityId, action);
    }

    private String extractEntityId(Object[] args, Object result) {
        for (Object arg : args) {
            if (arg instanceof java.util.UUID) {
                return arg.toString();
            }
        }
        return "unknown";
    }
}