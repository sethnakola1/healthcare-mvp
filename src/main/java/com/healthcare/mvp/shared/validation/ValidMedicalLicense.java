package com.healthcare.mvp.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MedicalLicenseValidator.class)
@Documented
public @interface ValidMedicalLicense {
    String message() default "Invalid medical license number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

