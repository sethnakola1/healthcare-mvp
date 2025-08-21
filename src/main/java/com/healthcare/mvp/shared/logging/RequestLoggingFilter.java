package com.healthcare.mvp.shared.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (shouldNotLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("remoteAddr", getClientIpAddress(request));

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            logRequest(requestWrapper, requestId);
            
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            long duration = System.currentTimeMillis() - startTime;
            logResponse(responseWrapper, requestId, duration);
            
        } finally {
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("requestId", requestId);
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            logData.put("queryString", request.getQueryString());
            logData.put("remoteAddr", getClientIpAddress(request));
            logData.put("userAgent", request.getHeader("User-Agent"));
            logData.put("contentType", request.getContentType());
            
            if (shouldLogRequestBody(request)) {
                String requestBody = new String(request.getContentAsByteArray());
                if (!requestBody.isEmpty()) {
                    logData.put("requestBody", maskSensitiveData(requestBody));
                }
            }
            
            log.info("HTTP_REQUEST: {}", objectMapper.writeValueAsString(logData));
            
        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("requestId", requestId);
            logData.put("status", response.getStatus());
            logData.put("duration", duration);
            logData.put("contentType", response.getContentType());
            
            if (response.getStatus() >= 400) {
                String responseBody = new String(response.getContentAsByteArray());
                if (!responseBody.isEmpty()) {
                    logData.put("responseBody", responseBody);
                }
            }
            
            if (duration > 1000) {
                log.warn("SLOW_REQUEST: {}", objectMapper.writeValueAsString(logData));
            } else {
                log.info("HTTP_RESPONSE: {}", objectMapper.writeValueAsString(logData));
            }
            
        } catch (Exception e) {
            log.error("Failed to log response", e);
        }
    }

    private boolean shouldNotLog(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/") ||
               uri.startsWith("/swagger-") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/webjars/");
    }

    private boolean shouldLogRequestBody(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        if (uri.contains("/auth/")) {
            return false;
        }
        
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private String maskSensitiveData(String requestBody) {
        return requestBody
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("\"currentPassword\"\\s*:\\s*\"[^\"]*\"", "\"currentPassword\":\"***\"")
                .replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"***\"")
                .replaceAll("\"confirmPassword\"\\s*:\\s*\"[^\"]*\"", "\"confirmPassword\":\"***\"")
                .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"")
                .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"***\"");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}