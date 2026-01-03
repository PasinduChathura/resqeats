package com.ffms.resqeats.enums.notification;

public enum NotificationType {
    ORDER_CREATED("ORDER_CREATED"),
    ORDER_ACCEPTED("ORDER_ACCEPTED"),
    ORDER_DECLINED("ORDER_DECLINED"),
    ORDER_CANCELLED("ORDER_CANCELLED"),
    NEW_ORDER("NEW_ORDER"),
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),
    PAYMENT_FAILED("PAYMENT_FAILED"),
    ORDER_PREPARING("ORDER_PREPARING"),
    ORDER_READY("ORDER_READY"),
    ORDER_PICKED_UP("ORDER_PICKED_UP"),
    ORDER_COMPLETED("ORDER_COMPLETED"),
    ORDER_EXPIRED("ORDER_EXPIRED"),
    SHOP_APPROVED("SHOP_APPROVED"),
    SHOP_REJECTED("SHOP_REJECTED"),
    SHOP_SUSPENDED("SHOP_SUSPENDED"),
    LOW_INVENTORY("LOW_INVENTORY");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
