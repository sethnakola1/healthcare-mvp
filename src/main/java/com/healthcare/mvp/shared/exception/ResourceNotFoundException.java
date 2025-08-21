package com.healthcare.mvp.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s with identifier '%s' not found", resourceType, identifier), 
              HttpStatus.NOT_FOUND);
    }
}