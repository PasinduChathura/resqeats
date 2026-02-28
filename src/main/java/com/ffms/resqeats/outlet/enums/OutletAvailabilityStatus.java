package com.ffms.resqeats.outlet.enums;

/**
 * Operational availability status for an outlet.
 *
 * <p>This is intentionally separate from {@link OutletStatus}, which represents the outlet
 * lifecycle (approval/suspension/disabled) and customer visibility.</p>
 */
public enum OutletAvailabilityStatus {
    /**
     * Outlet is manually marked as open (still subject to operating hours).
     */
    OPEN,

    /**
     * Outlet is manually marked as closed (stop accepting orders).
     */
    CLOSED
}
