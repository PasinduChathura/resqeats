package com.ffms.resqeats.merchant.enums;

/**
 * Merchant status per SRS Section 7.2.
 * BR-016: Merchants must be approved before creating outlets.
 */
public enum MerchantStatus {
    /**
     * Merchant registration is pending admin review.
     */
    PENDING_APPROVAL,

    /**
     * Merchant is approved and can operate.
     */
    APPROVED,

    /**
     * Merchant is suspended due to policy violations.
     * BR-018: Suspended merchants cannot process new orders.
     */
    SUSPENDED,

    /**
     * Merchant is disabled (soft-deactivated) and cannot operate.
     */
    DISABLED,

    /**
     * Merchant registration was rejected.
     */
    REJECTED
}
