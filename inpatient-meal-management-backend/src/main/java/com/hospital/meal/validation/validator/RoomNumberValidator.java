package com.hospital.meal.validation.validator;

import com.hospital.meal.util.ValidationUtil;
import com.hospital.meal.validation.annotation.ValidRoomNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoomNumberValidator implements ConstraintValidator<ValidRoomNumber, String> {

    @Override
    public void initialize(ValidRoomNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String roomNumber, ConstraintValidatorContext context) {
        return ValidationUtil.isValidRoomNumber(roomNumber);
    }
}