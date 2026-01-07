package com.ffms.resqeats.exception.common;

/**
 * Error codes for consistent error identification across the application.
 */
public final class ErrorCodes {

    private ErrorCodes() {}

    // ===================== Categories =====================
    public static final String CATEGORY_AUTH = "AUTH";
    public static final String CATEGORY_USER = "USER";
    public static final String CATEGORY_SHOP = "SHOP";
    public static final String CATEGORY_FOOD = "FOOD";
    public static final String CATEGORY_CART = "CART";
    public static final String CATEGORY_ORDER = "ORDER";
    public static final String CATEGORY_PAYMENT = "PAYMENT";
    public static final String CATEGORY_NOTIFICATION = "NOTIFICATION";
    public static final String CATEGORY_SYSTEM = "SYSTEM";
    public static final String CATEGORY_VALIDATION = "VALIDATION";

    // ===================== Auth Errors (AUTH_xxx) =====================
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_002";
    public static final String AUTH_TOKEN_INVALID = "AUTH_003";
    public static final String AUTH_REFRESH_TOKEN_EXPIRED = "AUTH_004";
    public static final String AUTH_REFRESH_TOKEN_INVALID = "AUTH_005";
    public static final String AUTH_ACCESS_DENIED = "AUTH_006";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_007";
    public static final String AUTH_ACCOUNT_DISABLED = "AUTH_008";
    public static final String AUTH_PASSWORD_RESET_EXPIRED = "AUTH_009";
    public static final String AUTH_PASSWORD_MISMATCH = "AUTH_010";

    // ===================== User Errors (USER_xxx) =====================
    public static final String USER_NOT_FOUND = "USER_001";
    public static final String USER_ALREADY_EXISTS = "USER_002";
    public static final String USER_EMAIL_EXISTS = "USER_003";
    public static final String USER_USERNAME_EXISTS = "USER_004";
    public static final String USER_INVALID_STATUS = "USER_005";
    public static final String USER_INVALID_TYPE = "USER_006";
    public static final String USER_ROLE_NOT_FOUND = "USER_007";

    // ===================== Shop Errors (SHOP_xxx) =====================
    public static final String SHOP_NOT_FOUND = "SHOP_001";
    public static final String SHOP_ACCESS_DENIED = "SHOP_002";
    public static final String SHOP_ALREADY_EXISTS = "SHOP_003";
    public static final String SHOP_INVALID_STATUS = "SHOP_004";
    public static final String SHOP_NOT_APPROVED = "SHOP_005";
    public static final String SHOP_SUSPENDED = "SHOP_006";
    public static final String SHOP_CLOSED = "SHOP_007";
    public static final String SHOP_INVALID_LOCATION = "SHOP_008";
    public static final String SHOP_INVALID_OPERATING_HOURS = "SHOP_009";

    // ===================== Food Item Errors (FOOD_xxx) =====================
    public static final String FOOD_ITEM_NOT_FOUND = "FOOD_001";
    public static final String FOOD_ITEM_ACCESS_DENIED = "FOOD_002";
    public static final String FOOD_ITEM_NOT_AVAILABLE = "FOOD_003";
    public static final String FOOD_ITEM_EXPIRED = "FOOD_004";

    // ===================== Secret Box Errors (SBOX_xxx) =====================
    public static final String SECRET_BOX_NOT_FOUND = "SBOX_001";
    public static final String SECRET_BOX_ACCESS_DENIED = "SBOX_002";
    public static final String SECRET_BOX_NOT_AVAILABLE = "SBOX_003";
    public static final String SECRET_BOX_EXPIRED = "SBOX_004";
    public static final String SECRET_BOX_INSUFFICIENT_QUANTITY = "SBOX_005";
    public static final String SECRET_BOX_INVALID_DISCOUNT = "SBOX_006";

    // ===================== Cart Errors (CART_xxx) =====================
    public static final String CART_NOT_FOUND = "CART_001";
    public static final String CART_EXPIRED = "CART_002";
    public static final String CART_EMPTY = "CART_003";
    public static final String CART_ITEM_NOT_FOUND = "CART_004";
    public static final String CART_MULTIPLE_SHOPS = "CART_005";
    public static final String CART_QUANTITY_EXCEEDED = "CART_006";
    public static final String CART_ITEM_UNAVAILABLE = "CART_007";

    // ===================== Order Errors (ORDER_xxx) =====================
    public static final String ORDER_NOT_FOUND = "ORDER_001";
    public static final String ORDER_ACCESS_DENIED = "ORDER_002";
    public static final String ORDER_INVALID_STATUS = "ORDER_003";
    public static final String ORDER_ALREADY_CANCELLED = "ORDER_004";
    public static final String ORDER_CANNOT_CANCEL = "ORDER_005";
    public static final String ORDER_EXPIRED = "ORDER_006";
    public static final String ORDER_PAYMENT_REQUIRED = "ORDER_007";
    public static final String ORDER_INVALID_PICKUP_CODE = "ORDER_008";
    public static final String ORDER_ALREADY_PICKED_UP = "ORDER_009";
    public static final String ORDER_PICKUP_WINDOW_CLOSED = "ORDER_010";
    public static final String ORDER_MULTIPLE_SHOPS = "ORDER_011";
    // LOW FIX (Issue #18): Added distinct error code for review period expired
    public static final String ORDER_REVIEW_PERIOD_EXPIRED = "ORDER_012";

    // ===================== Payment Errors (PAY_xxx) =====================
    public static final String PAYMENT_NOT_FOUND = "PAY_001";
    public static final String PAYMENT_METHOD_NOT_FOUND = "PAY_002";
    public static final String PAYMENT_FAILED = "PAY_003";
    public static final String PAYMENT_ALREADY_PROCESSED = "PAY_004";
    public static final String PAYMENT_INVALID_STATUS = "PAY_005";
    public static final String PAYMENT_PREAUTH_FAILED = "PAY_006";
    public static final String PAYMENT_CAPTURE_FAILED = "PAY_007";
    public static final String PAYMENT_REFUND_FAILED = "PAY_008";
    public static final String PAYMENT_VOID_FAILED = "PAY_009";
    public static final String PAYMENT_METHOD_INACTIVE = "PAY_010";
    public static final String PAYMENT_WEBHOOK_INVALID = "PAY_011";

    // ===================== Notification Errors (NOTIF_xxx) =====================
    public static final String NOTIFICATION_NOT_FOUND = "NOTIF_001";
    public static final String NOTIFICATION_SEND_FAILED = "NOTIF_002";

    // ===================== Validation Errors (VAL_xxx) =====================
    public static final String VALIDATION_FAILED = "VAL_001";
    public static final String VALIDATION_REQUIRED_FIELD = "VAL_002";
    public static final String VALIDATION_INVALID_FORMAT = "VAL_003";
    public static final String VALIDATION_OUT_OF_RANGE = "VAL_004";

    // ===================== System Errors (SYS_xxx) =====================
    public static final String SYSTEM_INTERNAL_ERROR = "SYS_001";
    public static final String SYSTEM_UNAVAILABLE = "SYS_002";
    public static final String SYSTEM_TIMEOUT = "SYS_003";
    public static final String SYSTEM_DATABASE_ERROR = "SYS_004";
    public static final String SYSTEM_EXTERNAL_SERVICE_ERROR = "SYS_005";
}
