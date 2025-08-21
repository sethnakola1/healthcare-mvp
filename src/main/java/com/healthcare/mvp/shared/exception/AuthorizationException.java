package com.healthcare.mvp.shared.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BusinessException {
    public AuthorizationException(String message) {
        super("AUTHORIZATION_FAILED", message, HttpStatus.FORBIDDEN);
    }
}