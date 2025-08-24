package com.healthcare.mvp.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {

    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private BaseResponse(boolean success, String message, T data, ErrorDetails error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data, null);
    }

    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(false, "An error occurred", null, new ErrorDetails(code, message));
    }

    @Getter
    @Setter
    private static class ErrorDetails implements Serializable {
        private String code;
        private String message;

        public ErrorDetails(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
