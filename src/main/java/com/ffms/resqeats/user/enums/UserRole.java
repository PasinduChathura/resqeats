package com.ffms.resqeats.user.enums;

/**
 * User roles per SRS Section 3.
 * Strict RBAC enforcement with hierarchical permissions.
 * 
 * Hierarchy: SUPER_ADMIN > ADMIN > MERCHANT_USER > OUTLET_USER > CUSTOMER_USER
 */
public enum UserRole {
    /**
     * Super administrator with unrestricted system access.
     * Can perform all operations including managing other admins.
     * All actions are audited.
     */
    SUPER_ADMIN(5),

    /**
     * Platform administrator with full system access.
     * Can manage all merchants, outlets, users, and view platform-wide analytics.
     * All actions are audited.
     */
    ADMIN(4),

    /**
     * Merchant owner who can manage multiple outlets.
     * Can create outlets, items, and outlet users.
     * Access limited to own merchant's data only.
     */
    MERCHANT_USER(3),

    /**
     * Outlet staff member assigned to a specific outlet.
     * Can manage orders, update item availability, and verify pickups.
     * Access limited to assigned outlet only.
     */
    OUTLET_USER(2),

    /**
     * End customer (mobile app user).
     * Can browse, order, pay, and pickup Secret Boxes.
     */
    CUSTOMER_USER(1);

    private final int hierarchyLevel;

    UserRole(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    /**
     * Check if this role has higher or equal authority than another role.
     */
    public boolean hasAuthorityOver(UserRole other) {
        return this.hierarchyLevel >= other.hierarchyLevel;
    }

    /**
     * Check if this role is at least the specified minimum role.
     */
    public boolean isAtLeast(UserRole minimumRole) {
        return this.hierarchyLevel >= minimumRole.hierarchyLevel;
    }

    /**
     * Check if this role requires audit logging.
     */
    public boolean requiresAudit() {
        return this == SUPER_ADMIN || this == ADMIN;
    }

    /**
     * Check if this role has tenant scope restrictions.
     */
    public boolean hasTenantScope() {
        return this == MERCHANT_USER || this == OUTLET_USER;
    }

    /**
     * Check if this role has global access (no tenant filtering).
     */
    public boolean hasGlobalAccess() {
        return this == SUPER_ADMIN || this == ADMIN;
    }
}
