package com.hospital.meal.constant;

import java.time.LocalTime;

public final class MealTimeConstants {

    private MealTimeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Breakfast time window: 7:00 AM - 9:00 AM
    public static final LocalTime BREAKFAST_START_TIME = LocalTime.of(7, 0);
    public static final LocalTime BREAKFAST_END_TIME = LocalTime.of(9, 0);

    // Lunch time window: 12:00 PM - 2:30 PM
    public static final LocalTime LUNCH_START_TIME = LocalTime.of(12, 0);
    public static final LocalTime LUNCH_END_TIME = LocalTime.of(14, 30);

    // Dinner time window: 6:30 PM - 8:30 PM
    public static final LocalTime DINNER_START_TIME = LocalTime.of(18, 30);
    public static final LocalTime DINNER_END_TIME = LocalTime.of(20, 30);

    /**
     * Check if current time is within the meal's modification window
     */
    public static boolean isWithinMealWindow(String mealType, LocalTime currentTime) {
        return switch (mealType) {
            case MealTypeConstants.BREAKFAST ->
                    !currentTime.isAfter(BREAKFAST_END_TIME);
            case MealTypeConstants.LUNCH ->
                    !currentTime.isAfter(LUNCH_END_TIME);
            case MealTypeConstants.DINNER ->
                    !currentTime.isAfter(DINNER_END_TIME);
            default -> false;
        };
    }

    /**
     * Get the end time for a specific meal type
     */
    public static LocalTime getMealEndTime(String mealType) {
        return switch (mealType) {
            case MealTypeConstants.BREAKFAST -> BREAKFAST_END_TIME;
            case MealTypeConstants.LUNCH -> LUNCH_END_TIME;
            case MealTypeConstants.DINNER -> DINNER_END_TIME;
            default -> null;
        };
    }
}