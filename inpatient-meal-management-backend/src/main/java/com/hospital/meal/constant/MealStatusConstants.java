package com.hospital.meal.constant;

import java.util.Arrays;
import java.util.List;

public final class MealStatusConstants {

    private MealStatusConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
    public static final String PROCESSED = "PROCESSED";

    public static final List<String> ALL_STATUSES = Arrays.asList(PENDING, PROCESSING, PROCESSED);

    public static boolean isValidStatus(String status) {
        return ALL_STATUSES.contains(status);
    }
}