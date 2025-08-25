package com.healthcare.mvp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse<T> {

    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private OffsetDateTime offsetTimestamp;
    private String path;
    private Integer status;
    private Map<String, Object> metadata;

    // FIXED: Single parameter success method
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // FIXED: Two parameter success method - message and data
    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Success message only
    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response builders
    public static <T> BaseResponse<T> error(String errorMessage) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorCode, String errorMessage) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .error(errorMessage)
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorMessage, Integer status) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorMessage, String path, Integer status) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .path(path)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Builder methods for complex responses
    public static <T> BaseResponse<T> errorWithCode(String errorCode, String message, String path) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> errorWithMetadata(String errorCode, String message,
                                                       Map<String, Object> metadata) {
        return BaseResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
}