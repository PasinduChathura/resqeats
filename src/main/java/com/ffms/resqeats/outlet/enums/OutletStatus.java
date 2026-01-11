package com.ffms.resqeats.outlet.enums;

/**
 * Outlet status per SRS Section 5.4 (FR-W-021).
 */
public enum OutletStatus {
    /**
     * Legacy/primary pending status used in seed data and API filters.
     */
    PENDING,

    /**
     * Outlet is operational and can accept orders.
     */
    ACTIVE,

    /**
     * Outlet is temporarily closed (vacation, maintenance).
     */
    TEMPORARILY_CLOSED,

    /**
     * Outlet is suspended by admin action.
     */
    SUSPENDED,

    /**
     * Legacy inactive status used in seed data.
     */
    INACTIVE
}
