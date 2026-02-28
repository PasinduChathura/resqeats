package com.ffms.resqeats.outlet.enums;

/**
 * Outlet status per SRS Section 5.4 (FR-W-021).
 */
public enum OutletStatus {
    /**
     * Outlet is pending approval.
     */
    PENDING_APPROVAL,

    /**
     * Outlet is operational and can accept orders.
     */
    ACTIVE,

    /**
     * Outlet is suspended by admin action.
     */
    SUSPENDED,

    /**
     * Outlet is disabled and hidden from customers.
     */
    DISABLED
}
