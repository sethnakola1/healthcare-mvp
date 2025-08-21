package com.healthcare.mvp.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

/**
 * Validator to ensure password and confirmPassword fields match
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            // Use reflection to get password and confirmPassword fields
            Field passwordField = obj.getClass().getDeclaredField("password");
            Field confirmPasswordField = obj.getClass().getDeclaredField("confirmPassword");

            passwordField.setAccessible(true);
            confirmPasswordField.setAccessible(true);

            String password = (String) passwordField.get(obj);
            String confirmPassword = (String) confirmPasswordField.get(obj);

            // If either is null, let other validators handle it
            if (password == null || confirmPassword == null) {
                return true;
            }

            // Check if they match
            boolean matches = password.equals(confirmPassword);

            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Passwords do not match")
                       .addPropertyNode("confirmPassword")
                       .addConstraintViolation();
            }

            return matches;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If fields don't exist or can't be accessed, consider it valid
            // Let other validators handle the actual field validation
            return true;
        }
    }
}