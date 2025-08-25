package com.healthcare.mvp.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s with identifier '%s' not found", resourceType, identifier),
              HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s with %s '%s' not found", resourceType, field, value),
              HttpStatus.NOT_FOUND);
    }
}