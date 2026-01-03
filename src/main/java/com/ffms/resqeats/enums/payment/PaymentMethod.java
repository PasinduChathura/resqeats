package com.ffms.resqeats.enums.payment;

public enum PaymentMethod {
    CARD("CARD"),
    WALLET("WALLET"),
    BANK_TRANSFER("BANK_TRANSFER");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
