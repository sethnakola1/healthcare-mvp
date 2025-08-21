package com.healthcare.mvp.shared.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceMonitor {

    private static final org.slf4j.Logger performanceLogger = 
        org.slf4j.LoggerFactory.getLogger("com.healthcare.mvp.performance");

    @Around("@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.web.bind.annotation.RestController)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String operationName = className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        
        try {
            MDC.put("operation", operationName);
            MDC.put("startTime", String.valueOf(startTime));
            
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {
                performanceLogger.warn("SLOW_OPERATION: {} took {}ms", operationName, executionTime);
            } else if (executionTime > 500) {
                performanceLogger.info("MODERATE_OPERATION: {} took {}ms", operationName, executionTime);
            }
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "SUCCESS");
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            performanceLogger.error("FAILED_OPERATION: {} failed after {}ms - {}", 
                operationName, executionTime, e.getMessage());
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "ERROR");
            MDC.put("error", e.getMessage());
            
            throw e;
            
        } finally {
            MDC.clear();
        }
    }
}