package com.hospital.meal.constant;

import java.util.Arrays;
import java.util.List;

public final class MealTypeConstants {

    private MealTypeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String BREAKFAST = "BREAKFAST";
    public static final String LUNCH = "LUNCH";
    public static final String DINNER = "DINNER";
    public static final String EXTRA = "EXTRA";

    public static final List<String> MAIN_MEAL_TYPES = Arrays.asList(BREAKFAST, LUNCH, DINNER);
    public static final List<String> ALL_MEAL_TYPES = Arrays.asList(BREAKFAST, LUNCH, DINNER, EXTRA);

    public static boolean isValidMealType(String mealType) {
        return ALL_MEAL_TYPES.contains(mealType);
    }

    public static boolean isMainMealType(String mealType) {
        return MAIN_MEAL_TYPES.contains(mealType);
    }
}