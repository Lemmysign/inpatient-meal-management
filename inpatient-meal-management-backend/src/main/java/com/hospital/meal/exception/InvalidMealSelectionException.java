package com.hospital.meal.exception;

import java.util.List;

public class InvalidMealSelectionException extends RuntimeException {

    public InvalidMealSelectionException(String message) {
        super(message);
    }

    public InvalidMealSelectionException(List<String> requiredMeals, List<String> providedMeals) {
        super(String.format(
                "Invalid meal selection. Required meals: %s. You provided: %s. " +
                        "Please ensure you order all required meals for this time period.",
                String.join(", ", requiredMeals),
                providedMeals.isEmpty() ? "none" : String.join(", ", providedMeals)
        ));
    }
}