package com.hospital.meal.validation.validator;

import com.hospital.meal.util.ValidationUtil;
import com.hospital.meal.validation.annotation.ValidUHID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UHIDValidator implements ConstraintValidator<ValidUHID, String> {

    @Override
    public void initialize(ValidUHID constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String uhid, ConstraintValidatorContext context) {
        return ValidationUtil.isValidUHID(uhid);
    }
}