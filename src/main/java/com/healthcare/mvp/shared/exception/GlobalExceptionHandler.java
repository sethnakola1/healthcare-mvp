package com.healthcare.mvp.shared.exception;

import com.healthcare.mvp.shared.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .data(ex.getData())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fieldErrors", ex.getFieldErrors());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .metadata(metadata)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage(),
                    (existing, replacement) -> existing
                ));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fieldErrors", fieldErrors);
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .metadata(metadata)
                .build();
        
        log.warn("Validation error: {}", fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Object>> handleBindException(BindException ex, WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage(),
                    (existing, replacement) -> existing
                ));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fieldErrors", fieldErrors);

        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .message("Request parameter validation failed")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .metadata(metadata)
                .build();

        log.warn("Bind validation error: {}", fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing
                ));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fieldErrors", fieldErrors);
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("CONSTRAINT_VIOLATION")
                .message("Constraint validation failed")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .metadata(metadata)
                .build();
        
        log.warn("Constraint violation: {}", fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("ACCESS_DENIED")
                .message("You don't have permission to access this resource")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        String userMessage = "Data operation failed. Please check for duplicate entries or invalid references.";
        String errorCode = "DATA_INTEGRITY_ERROR";
        
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        if (rootMessage != null) {
            if (rootMessage.contains("duplicate key") || rootMessage.contains("UNIQUE constraint")) {
                userMessage = "Record with this information already exists";
                errorCode = "DUPLICATE_ENTRY";
            } else if (rootMessage.contains("foreign key constraint")) {
                userMessage = "Referenced record not found or cannot be deleted due to dependencies";
                errorCode = "FOREIGN_KEY_VIOLATION";
            }
        }
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(userMessage)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.warn("Malformed JSON request: {}", ex.getMessage());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("MALFORMED_REQUEST")
                .message("Invalid JSON format in request body")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value for parameter '%s'. Expected type: %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("TYPE_MISMATCH")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Object>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        log.warn("Missing required parameter: {}", ex.getParameterName());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("MISSING_PARAMETER")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        log.warn("Method not supported: {}", ex.getMethod());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("METHOD_NOT_SUPPORTED")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        log.warn("Media type not supported: {}", ex.getContentType());
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("MEDIA_TYPE_NOT_SUPPORTED")
                .message("Content type not supported. Please use application/json")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .errorCode("AUTHENTICATION_FAILED")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


}