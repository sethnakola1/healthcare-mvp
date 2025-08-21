package com.healthcare.mvp.shared.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceType, String field, String value) {
        super("DUPLICATE_RESOURCE", 
              String.format("%s with %s '%s' already exists", resourceType, field, value), 
              HttpStatus.CONFLICT);
    }
}