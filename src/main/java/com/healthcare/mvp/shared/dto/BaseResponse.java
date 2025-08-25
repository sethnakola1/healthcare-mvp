package com.healthcare.mvp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private int statusCode;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Success")
                .timestamp(java.time.Instant.now().toString())
                .statusCode(200)
                .build();
    }

    public static <T> BaseResponse<T> error(String message, int statusCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(java.time.Instant.now().toString())
                .statusCode(statusCode)
                .build();
    }
}