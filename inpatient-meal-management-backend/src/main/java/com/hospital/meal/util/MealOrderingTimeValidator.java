package com.hospital.meal.util;

import com.hospital.meal.constant.MealTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MealOrderingTimeValidator {

    // Morning ordering window: 9:00 PM - 9:00 AM
    private static final LocalTime MORNING_WINDOW_START = LocalTime.of(21, 0); // 9:00 PM
    private static final LocalTime MORNING_WINDOW_END = LocalTime.of(9, 0);    // 9:00 AM

    // Lunch ordering window: 10:00 AM - 3:00 PM
    private static final LocalTime LUNCH_WINDOW_START = LocalTime.of(10, 0);   // 10:00 AM
    private static final LocalTime LUNCH_WINDOW_END = LocalTime.of(15, 0);     // 3:00 PM

    // Dinner ordering window: 5:00 PM - 7:00 PM
    private static final LocalTime DINNER_WINDOW_START = LocalTime.of(17, 0);  // 5:00 PM
    private static final LocalTime DINNER_WINDOW_END = LocalTime.of(19, 0);    // 7:00 PM

    /**
     * Get required meal types based on current time
     *
     * @param currentTime Current time in Africa/Lagos timezone
     * @return List of required meal types
     */
    public List<String> getRequiredMealTypes(LocalTime currentTime) {
        log.debug("Checking required meals for time: {}", currentTime);

        // Morning window (9:00 PM - 10:00 AM)
        if (isInMorningWindow(currentTime)) {
            log.debug("Morning ordering window - all meals required");
            return Arrays.asList(
                    MealTypeConstants.BREAKFAST,
                    MealTypeConstants.LUNCH,
                    MealTypeConstants.DINNER
            );
        }

        // Lunch window (10:00 AM - 3:00 PM)
        if (isInLunchWindow(currentTime)) {
            log.debug("Lunch ordering window - lunch and dinner required");
            return Arrays.asList(
                    MealTypeConstants.LUNCH,
                    MealTypeConstants.DINNER
            );
        }

        // Dinner window (5:00 PM - 7:00 PM)
        if (isInDinnerWindow(currentTime)) {
            log.debug("Dinner ordering window - dinner only required");
            return Arrays.asList(MealTypeConstants.DINNER);
        }

        // Outside all windows
        log.warn("Current time {} is outside all ordering windows", currentTime);
        return Arrays.asList(); // Empty list = no ordering allowed
    }

    /**
     * Check if ordering is allowed at current time
     */
    public boolean isOrderingAllowed(LocalTime currentTime) {
        return isInMorningWindow(currentTime)
                || isInLunchWindow(currentTime)
                || isInDinnerWindow(currentTime);
    }

    /**
     * Get user-friendly message about ordering window
     */
    public String getOrderingWindowMessage(LocalTime currentTime) {
        if (isInMorningWindow(currentTime)) {
            return "Morning ordering period (9:00 PM - 9:00 AM). You must order Breakfast, Lunch, and Dinner.";
        }

        if (isInLunchWindow(currentTime)) {
            return "Lunch ordering period (11:00 AM - 3:00 PM). You must order Lunch and Dinner.";
        }

        if (isInDinnerWindow(currentTime)) {
            return "Dinner ordering period (5:00 PM - 7:00 PM). You must order Dinner only.";
        }

        return "Ordering is currently closed. Please order during: " +
                "Morning (9:00 PM - 9:00 AM), Lunch (11:00 AM - 3:00 PM), or Dinner (5:00 PM - 7:00 PM).";
    }

    /**
     * Get next available ordering window
     */
    public String getNextOrderingWindow(LocalTime currentTime) {
        if (currentTime.isAfter(DINNER_WINDOW_END) && currentTime.isBefore(MORNING_WINDOW_START)) {
            return "Next ordering window: Tonight at 9:00 PM (Morning orders)";
        }

        if (currentTime.isAfter(MORNING_WINDOW_END) && currentTime.isBefore(LUNCH_WINDOW_START)) {
            return "Next ordering window: Today at 11:00 AM (Lunch orders)";
        }

        if (currentTime.isAfter(LUNCH_WINDOW_END) && currentTime.isBefore(DINNER_WINDOW_START)) {
            return "Next ordering window: Today at 5:00 PM (Dinner orders)";
        }

        return "Next ordering window: Tomorrow at 9:00 PM (Morning orders)";
    }

    // Helper methods
    private boolean isInMorningWindow(LocalTime time) {
        // Morning window crosses midnight (9 PM to 9 AM)
        return time.isAfter(MORNING_WINDOW_START) || time.isBefore(MORNING_WINDOW_END) || time.equals(MORNING_WINDOW_START);
    }

    private boolean isInLunchWindow(LocalTime time) {
        return (time.isAfter(LUNCH_WINDOW_START) || time.equals(LUNCH_WINDOW_START))
                && time.isBefore(LUNCH_WINDOW_END);
    }

    private boolean isInDinnerWindow(LocalTime time) {
        return (time.isAfter(DINNER_WINDOW_START) || time.equals(DINNER_WINDOW_START))
                && time.isBefore(DINNER_WINDOW_END);
    }
}