package com.hospital.meal.constant;

public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // API Version
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;

    // Auth endpoints
    public static final String AUTH_BASE = API_BASE_PATH + "/auth";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_PATIENT_LOGIN = "/patient/login";
    public static final String AUTH_SET_PASSWORD = "/set-password";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_FORGOT_PASSWORD = "/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";
    public static final String AUTH_VALIDATE_RESET_TOKEN = "/validate-reset-token";
    public static final String ORDER_BASE = "/api/orders";  // use your actual path

    // Admin endpoints
    public static final String ADMIN_BASE = API_BASE_PATH + "/admin";
    public static final String ADMIN_DIETICIANS = "/dieticians";
    public static final String ADMIN_KITCHEN_STAFF = "/kitchen-staff";
    public static final String ADMIN_DASHBOARD = "/dashboard";

    // Dietician endpoints
    public static final String DIETICIAN_BASE = API_BASE_PATH + "/dietician";
    public static final String DIETICIAN_MENU_GROUPS = "/menu-groups";
    public static final String DIETICIAN_FOOD_ITEMS = "/food-items";
    public static final String DIETICIAN_PATIENT_MENUS = "/patient-menus";
    public static final String DIETICIAN_DASHBOARD = "/dashboard";

    // Patient endpoints
    public static final String PATIENT_BASE = API_BASE_PATH + "/patient";
    public static final String PATIENT_MENU = "/menu";
    public static final String PATIENT_ORDERS = "/orders";
    public static final String PATIENT_ORDER_HISTORY = "/orders/history";

    // Kitchen endpoints
    public static final String KITCHEN_BASE = API_BASE_PATH + "/kitchen";
    public static final String KITCHEN_QUEUE = "/queue";
    public static final String KITCHEN_PROCESS = "/process";
    public static final String KITCHEN_PRINT = "/print";
    public static final String KITCHEN_DASHBOARD = "/dashboard";

    // Web Push endpoints
    public static final String WEB_PUSH_BASE = API_BASE_PATH + "/notifications";
    public static final String WEB_PUSH_SUBSCRIBE = "/subscribe";
    public static final String WEB_PUSH_UNSUBSCRIBE = "/unsubscribe";

    // Pagination defaults
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    // Session
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Rate limiting
    public static final int PATIENT_LOGIN_RATE_LIMIT = 10; // per minute
    public static final int PATIENT_ORDER_RATE_LIMIT = 5;  // per minute
}