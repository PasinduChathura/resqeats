package com.ffms.resqeats.enums.order;

public enum OrderStatus {
    CREATED("CREATED"),
    PENDING_SHOP_ACCEPTANCE("PENDING_SHOP_ACCEPTANCE"),
    PAID("PAID"),
    PREPARING("PREPARING"),
    READY_FOR_PICKUP("READY_FOR_PICKUP"),
    PICKED_UP("PICKED_UP"),
    COMPLETED("COMPLETED"),
    DECLINED("DECLINED"),
    CANCELLED("CANCELLED"),
    EXPIRED("EXPIRED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == PENDING_SHOP_ACCEPTANCE || newStatus == CANCELLED;
            case PENDING_SHOP_ACCEPTANCE -> newStatus == PAID || newStatus == DECLINED || newStatus == EXPIRED;
            case PAID -> newStatus == PREPARING || newStatus == CANCELLED;
            case PREPARING -> newStatus == READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> newStatus == PICKED_UP || newStatus == EXPIRED;
            case PICKED_UP -> newStatus == COMPLETED;
            default -> false;
        };
    }
}
