package com.ffms.resqeats.enums.shop;

public enum ShopStatus {
    PENDING_APPROVAL("PENDING_APPROVAL"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    SUSPENDED("SUSPENDED"),
    CLOSED("CLOSED");

    private final String value;

    ShopStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
