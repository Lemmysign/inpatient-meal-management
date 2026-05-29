package com.hospital.meal.constant;

import java.util.Arrays;
import java.util.List;

public final class RoleConstants {

    private RoleConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String ADMIN = "ADMIN";
    public static final String DIETICIAN = "DIETICIAN";
    public static final String KITCHEN_STAFF = "KITCHEN_STAFF";
    public static final String PATIENT = "PATIENT";

    public static final List<String> ALL_ROLES = Arrays.asList(ADMIN, DIETICIAN, KITCHEN_STAFF, PATIENT);

    public static final String ROLE_PREFIX = "ROLE_";

    public static String withPrefix(String role) {
        return ROLE_PREFIX + role;
    }

    public static boolean isValidRole(String role) {
        return ALL_ROLES.contains(role);
    }
}