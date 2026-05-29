package com.hospital.meal.util;

import com.hospital.meal.constant.ValidationConstants;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Pattern UHID_PATTERN = Pattern.compile(ValidationConstants.UHID_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(ValidationConstants.PHONE_PATTERN);
    private static final Pattern ROOM_NUMBER_PATTERN = Pattern.compile(ValidationConstants.ROOM_NUMBER_PATTERN);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(ValidationConstants.EMAIL_PATTERN);

    /**
     * Validate UHID (must be numeric only)
     */
    public static boolean isValidUHID(String uhid) {
        if (uhid == null || uhid.trim().isEmpty()) {
            return false;
        }

        String trimmed = uhid.trim();

        if (trimmed.length() < ValidationConstants.UHID_MIN_LENGTH ||
                trimmed.length() > ValidationConstants.UHID_MAX_LENGTH) {
            return false;
        }

        return UHID_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Check if string is numeric
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Phone is optional in many cases
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Validate room number
     */
    public static boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return false;
        }
        return ROOM_NUMBER_PATTERN.matcher(roomNumber.trim()).matches();
    }

    /**
     * Validate email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Sanitize string (remove extra spaces, trim)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Check if string is blank (null, empty, or only whitespace)
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}