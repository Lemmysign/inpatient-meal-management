package com.hospital.meal.validation.annotation;

import com.hospital.meal.validation.validator.RoomNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoomNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoomNumber {

    String message() default "Room number must contain only letters, numbers, and hyphens";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}