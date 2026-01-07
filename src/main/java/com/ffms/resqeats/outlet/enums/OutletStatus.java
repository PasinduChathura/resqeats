package com.ffms.resqeats.outlet.enums;

/**
 * Outlet status per SRS Section 5.4 (FR-W-021).
 */
public enum OutletStatus {
    /**
     * New outlet pending admin approval.
     */
    PENDING_APPROVAL,

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
     * Outlet is permanently deactivated.
     */
    DEACTIVATED
}
