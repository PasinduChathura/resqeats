package com.ffms.resqeats.enums.cart;

public enum CartStatus {
    ACTIVE("ACTIVE"),
    EXPIRED("EXPIRED"),
    CONVERTED("CONVERTED"),
    ABANDONED("ABANDONED");

    private final String value;

    CartStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
