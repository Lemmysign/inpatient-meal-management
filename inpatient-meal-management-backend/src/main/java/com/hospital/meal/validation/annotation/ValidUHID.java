package com.hospital.meal.validation.annotation;

import com.hospital.meal.validation.validator.UHIDValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UHIDValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUHID {

    String message() default "UHID must be numeric and between 1-20 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}