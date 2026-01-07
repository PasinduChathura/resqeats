package com.ffms.resqeats.notification.enums;

/**
 * Notification delivery channels per SRS Section 6.12.
 */
public enum NotificationChannel {
    /**
     * Push notification via FCM/APNS.
     */
    PUSH,

    /**
     * WebSocket real-time message.
     */
    WEBSOCKET,

    /**
     * Email notification.
     */
    EMAIL,

    /**
     * SMS notification.
     */
    SMS,

    /**
     * In-app notification only.
     */
    IN_APP
}
