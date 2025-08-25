// src/main/java/com/healthcare/mvp/shared/validation/MedicalLicenseValidator.java
package com.healthcare.mvp.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class MedicalLicenseValidator implements ConstraintValidator<ValidMedicalLicense, String> {
    
    private static final Pattern MEDICAL_LICENSE_PATTERN = Pattern.compile("^[A-Z]{2}\\d{6,8}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        return MEDICAL_LICENSE_PATTERN.matcher(value.trim().toUpperCase()).matches();
    }
}