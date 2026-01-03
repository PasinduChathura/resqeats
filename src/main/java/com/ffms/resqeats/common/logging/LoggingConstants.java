package com.ffms.resqeats.common.logging;

/**
 * Centralized logging constants for consistent log messages across the application.
 */
public final class LoggingConstants {

    private LoggingConstants() {
        // Prevent instantiation
    }

    // ===================== Operation Types =====================
    public static final String OP_CREATE = "CREATE";
    public static final String OP_READ = "READ";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";
    public static final String OP_LOGIN = "LOGIN";
    public static final String OP_LOGOUT = "LOGOUT";
    public static final String OP_VALIDATE = "VALIDATE";
    public static final String OP_PROCESS = "PROCESS";
    public static final String OP_SEARCH = "SEARCH";
    public static final String OP_EXPORT = "EXPORT";
    public static final String OP_IMPORT = "IMPORT";

    // ===================== Entity Types =====================
    public static final String ENTITY_USER = "User";
    public static final String ENTITY_ROLE = "Role";
    public static final String ENTITY_PRIVILEGE = "Privilege";
    public static final String ENTITY_SHOP = "Shop";
    public static final String ENTITY_FOOD_ITEM = "FoodItem";
    public static final String ENTITY_SECRET_BOX = "SecretBox";
    public static final String ENTITY_CART = "Cart";
    public static final String ENTITY_CART_ITEM = "CartItem";
    public static final String ENTITY_ORDER = "Order";
    public static final String ENTITY_PAYMENT = "Payment";
    public static final String ENTITY_PAYMENT_METHOD = "PaymentMethod";
    public static final String ENTITY_NOTIFICATION = "Notification";
    public static final String ENTITY_REFRESH_TOKEN = "RefreshToken";
    public static final String ENTITY_PASSWORD_RESET_TOKEN = "PasswordResetToken";

    // ===================== Log Formats =====================
    public static final String LOG_ENTRY = "[{}] Starting {} for {}";
    public static final String LOG_EXIT = "[{}] Completed {} for {} - Duration: {}ms";
    public static final String LOG_ERROR = "[{}] Error during {} for {}: {}";
    public static final String LOG_WARN = "[{}] Warning during {} for {}: {}";

    // ===================== Status =====================
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_PARTIAL = "PARTIAL";
}
