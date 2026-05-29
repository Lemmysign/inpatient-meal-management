package com.hospital.meal.util;

import com.hospital.meal.constant.MealTimeConstants;
import com.hospital.meal.constant.MealTypeConstants;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class MealTimeValidator {

    /**
     * Check if a meal can be modified based on current time
     *
     * Rules:
     * - Breakfast can only be changed before 9:00 AM
     * - Lunch can only be changed before 2:30 PM
     * - Dinner can only be changed before 8:30 PM
     * - Extra meals can always be modified
     */
    public boolean canModifyMeal(String mealType) {
        if (mealType == null) {
            return false;
        }

        // Extra meals can always be modified
        if (MealTypeConstants.EXTRA.equals(mealType)) {
            return true;
        }

        LocalTime currentTime = LocalTime.now();

        return MealTimeConstants.isWithinMealWindow(mealType, currentTime);
    }

    /**
     * Check if a meal can be modified with a specific reason
     */
    public boolean canModifyMeal(String mealType, LocalTime currentTime) {
        if (mealType == null || currentTime == null) {
            return false;
        }

        // Extra meals can always be modified
        if (MealTypeConstants.EXTRA.equals(mealType)) {
            return true;
        }

        return MealTimeConstants.isWithinMealWindow(mealType, currentTime);
    }

    /**
     * Get the deadline time for modifying a meal
     */
    public LocalTime getModificationDeadline(String mealType) {
        return MealTimeConstants.getMealEndTime(mealType);
    }

    /**
     * Get a human-readable message for why modification is not allowed
     */
    public String getModificationDeniedMessage(String mealType) {
        LocalTime deadline = getModificationDeadline(mealType);

        if (deadline == null) {
            return "This meal type cannot be modified";
        }

        return String.format("Modification deadline for %s has passed (deadline: %s)",
                mealType.toLowerCase(),
                deadline.format(DateUtil.TIME_FORMATTER));
    }

    /**
     * Get minutes remaining until modification deadline
     */
    public long getMinutesUntilDeadline(String mealType) {
        LocalTime deadline = getModificationDeadline(mealType);
        if (deadline == null) {
            return -1;
        }

        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(deadline)) {
            return 0;
        }

        return java.time.Duration.between(currentTime, deadline).toMinutes();
    }
}