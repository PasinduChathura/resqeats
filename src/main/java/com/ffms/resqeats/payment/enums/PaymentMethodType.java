package com.ffms.resqeats.payment.enums;

/**
 * Payment method types per SRS Section 4.5 (FR-U-026).
 */
public enum PaymentMethodType {
    /**
     * Credit/Debit card payment.
     */
    CARD("CARD"),

    /**
     * Digital wallet (Apple Pay, Google Pay).
     */
    WALLET("WALLET"),

    /**
     * Bank transfer.
     */
    BANK_TRANSFER("BANK_TRANSFER");

    private final String value;

    PaymentMethodType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
