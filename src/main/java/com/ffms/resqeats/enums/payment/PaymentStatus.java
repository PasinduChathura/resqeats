package com.ffms.resqeats.enums.payment;

public enum PaymentStatus {
    PENDING("PENDING"),
    AUTHORIZED("AUTHORIZED"),
    CAPTURED("CAPTURED"),
    RELEASED("RELEASED"),
    FAILED("FAILED"),
    REFUNDED("REFUNDED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
