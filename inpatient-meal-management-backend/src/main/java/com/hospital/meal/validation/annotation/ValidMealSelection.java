package com.hospital.meal.validation.annotation;

import com.hospital.meal.validation.validator.MealSelectionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MealSelectionValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMealSelection {

    String message() default "Must select breakfast, lunch, and dinner";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}