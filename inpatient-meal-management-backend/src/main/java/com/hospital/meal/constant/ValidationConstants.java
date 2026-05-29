package com.hospital.meal.constant;

public final class ValidationConstants {

    private ValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // UHID validation
    public static final String UHID_PATTERN = "^[0-9]+$";
    public static final String UHID_MESSAGE = "UHID must contain only numeric characters";
    public static final int UHID_MIN_LENGTH = 1;
    public static final int UHID_MAX_LENGTH = 20;

    // Password validation
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;

    // Name validation
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 255;

    // Phone number validation
    public static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$";
    public static final String PHONE_MESSAGE = "Phone number must be valid (E.164 format)";

    // Room number validation
    public static final String ROOM_NUMBER_PATTERN = "^[A-Za-z0-9-]+$";
    public static final String ROOM_NUMBER_MESSAGE = "Room number must contain only letters, numbers, and hyphens";

    // Email validation
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}