package com.ffms.resqeats.order.enums;

/**
 * Order status per SRS Section 4.6 (FR-M-033).
 * Implements strict state machine with valid transitions.
 *
 * State Machine:
 * CREATED → PENDING_OUTLET_ACCEPTANCE (on order submission)
 * PENDING_OUTLET_ACCEPTANCE → PAID (on outlet accept + payment capture)
 * PENDING_OUTLET_ACCEPTANCE → DECLINED (on outlet decline)
 * PENDING_OUTLET_ACCEPTANCE → CANCELLED (on timeout)
 * PAID → PREPARING (on outlet action)
 * PREPARING → READY_FOR_PICKUP (on outlet action)
 * READY_FOR_PICKUP → PICKED_UP (on verification)
 * PICKED_UP → COMPLETED (automatic after delay)
 * READY_FOR_PICKUP → EXPIRED (on pickup window expiry)
 */
public enum OrderStatus {
    /**
     * Order created but not yet submitted for outlet acceptance.
     */
    CREATED,

    /**
     * Order submitted, waiting for outlet to accept.
     * Payment is pre-authorized at this stage.
     */
    PENDING_OUTLET_ACCEPTANCE,

    /**
     * Outlet accepted, payment captured, inventory decremented.
     */
    PAID,

    /**
     * Outlet is preparing the order.
     */
    PREPARING,

    /**
     * Order is ready for customer pickup.
     */
    READY_FOR_PICKUP,

    /**
     * Customer has picked up the order.
     */
    PICKED_UP,

    /**
     * Order completed successfully (terminal state).
     */
    COMPLETED,

    /**
     * Outlet declined the order (terminal state).
     * Pre-authorization is released.
     */
    DECLINED,

    /**
     * Order cancelled by customer or system (terminal state).
     */
    CANCELLED,

    /**
     * Order expired due to missed pickup window (terminal state).
     */
    EXPIRED,

    /**
     * Order refunded (terminal state).
     */
    REFUNDED;

    /**
     * Validate if transition to new status is allowed.
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == PENDING_OUTLET_ACCEPTANCE || newStatus == CANCELLED;
            case PENDING_OUTLET_ACCEPTANCE -> newStatus == PAID || newStatus == DECLINED || newStatus == CANCELLED;
            case PAID -> newStatus == PREPARING || newStatus == REFUNDED;
            case PREPARING -> newStatus == READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> newStatus == PICKED_UP || newStatus == EXPIRED;
            case PICKED_UP -> newStatus == COMPLETED;
            case COMPLETED, DECLINED, CANCELLED, EXPIRED, REFUNDED -> false; // Terminal states
        };
    }

    /**
     * Check if this is a terminal (final) state.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == DECLINED || this == CANCELLED || 
               this == EXPIRED || this == REFUNDED;
    }

    /**
     * Check if order is active (not terminal).
     */
    public boolean isActive() {
        return !isTerminal();
    }
}
