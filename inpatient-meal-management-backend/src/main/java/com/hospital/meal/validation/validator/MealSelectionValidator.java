package com.hospital.meal.validation.validator;

import com.hospital.meal.constant.MealTypeConstants;
import com.hospital.meal.dto.patient.CreateMealOrderRequest;
import com.hospital.meal.dto.patient.MealSelectionRequest;
import com.hospital.meal.validation.annotation.ValidMealSelection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealSelectionValidator implements ConstraintValidator<ValidMealSelection, CreateMealOrderRequest> {

    @Override
    public void initialize(ValidMealSelection constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CreateMealOrderRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getMeals() == null || request.getMeals().isEmpty()) {
            return false;
        }

        List<MealSelectionRequest> meals = request.getMeals();

        Set<String> mealTypes = new HashSet<>();
        for (MealSelectionRequest meal : meals) {
            if (meal.getMealType() != null) {
                mealTypes.add(meal.getMealType());
            }
        }

        // Must have all three main meals: BREAKFAST, LUNCH, DINNER
        boolean hasBreakfast = mealTypes.contains(MealTypeConstants.BREAKFAST);
        boolean hasLunch = mealTypes.contains(MealTypeConstants.LUNCH);
        boolean hasDinner = mealTypes.contains(MealTypeConstants.DINNER);

        if (!hasBreakfast || !hasLunch || !hasDinner) {
            context.disableDefaultConstraintViolation();

            StringBuilder message = new StringBuilder("Missing required meals: ");
            if (!hasBreakfast) message.append("BREAKFAST ");
            if (!hasLunch) message.append("LUNCH ");
            if (!hasDinner) message.append("DINNER");

            context.buildConstraintViolationWithTemplate(message.toString())
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}