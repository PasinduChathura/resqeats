package com.ffms.resqeats.payment.enums;

/**
 * Payment status per SRS Section 7.2.
 * Reflects the pre-authorization → capture flow.
 */
public enum PaymentStatus {
    /**
     * Payment initiated but not yet processed.
     */
    PENDING,

    /**
     * Payment pre-authorized (funds held but not captured).
     * BR-001: Orders cannot be placed without successful pre-authorization.
     */
    AUTHORIZED,

    /**
     * Payment captured (funds transferred).
     * BR-004: Payment is captured only upon outlet acceptance.
     */
    CAPTURED,

    /**
     * Pre-authorization voided (funds released).
     * BR-005: Pre-authorization is voided if outlet declines or times out.
     */
    VOIDED,

    /**
     * Payment refunded to customer.
     */
    REFUNDED,

    /**
     * Payment failed.
     */
    FAILED
}
