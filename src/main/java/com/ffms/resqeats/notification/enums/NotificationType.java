package com.ffms.resqeats.notification.enums;

/**
 * Notification types per SRS Section 6.12.
 */
public enum NotificationType {
    // Order-related
    ORDER_CREATED,
    ORDER_ACCEPTED,
    ORDER_DECLINED,
    ORDER_PREPARING,
    ORDER_READY,
    PICKUP_REMINDER,
    PICKUP_EXPIRED,
    
    // Payment-related
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_PROCESSED,
    
    // Merchant/Outlet related
    MERCHANT_APPROVED,
    MERCHANT_REJECTED,
    OUTLET_APPROVED,
    
    // System
    SYSTEM_ANNOUNCEMENT,
    ACCOUNT_UPDATE
}
