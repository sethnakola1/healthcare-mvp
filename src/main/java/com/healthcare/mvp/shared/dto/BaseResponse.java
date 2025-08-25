package com.healthcare.mvp.shared.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
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
    private Integer status;  // FIXED: Added status field
    private Map<String, Object> metadata;

    // Success response builders
    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return success("Operation completed successfully", data);
    }

    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // FIXED: Error response builders - use explicit type parameters to avoid ambiguity
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

    public static <T> BaseResponse<T> error(String errorMessage, Integer statusCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .status(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(String errorMessage, String path, Integer statusCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .error(errorMessage)
                .message(errorMessage)
                .path(path)
                .status(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // FIXED: Add statusCode method to builder to resolve compilation error
    public static class BaseResponseBuilder<T> {
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

        BaseResponseBuilder() {
        }

        public BaseResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public BaseResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public BaseResponseBuilder<T> errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public BaseResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public BaseResponseBuilder<T> error(String error) {
            this.error = error;
            return this;
        }

        public BaseResponseBuilder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BaseResponseBuilder<T> offsetTimestamp(OffsetDateTime offsetTimestamp) {
            this.offsetTimestamp = offsetTimestamp;
            return this;
        }

        public BaseResponseBuilder<T> path(String path) {
            this.path = path;
            return this;
        }

        // FIXED: Add the missing statusCode method
        public BaseResponseBuilder<T> statusCode(int statusCode) {
            this.status = statusCode;
            return this;
        }

        public BaseResponseBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        public BaseResponseBuilder<T> metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public BaseResponse<T> build() {
            return new BaseResponse<>(success, message, errorCode, data, error, timestamp, offsetTimestamp, path, status, metadata);
        }

        @Override
        public String toString() {
            return "BaseResponse.BaseResponseBuilder(success=" + this.success + ", message=" + this.message + ", errorCode=" + this.errorCode + ", data=" + this.data + ", error=" + this.error + ", timestamp=" + this.timestamp + ", offsetTimestamp=" + this.offsetTimestamp + ", path=" + this.path + ", status=" + this.status + ", metadata=" + this.metadata + ")";
        }
    }

    public static <T> BaseResponseBuilder<T> builder() {
        return new BaseResponseBuilder<>();
    }
}