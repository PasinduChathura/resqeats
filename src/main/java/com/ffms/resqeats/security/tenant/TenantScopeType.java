package com.ffms.resqeats.security.tenant;

/**
 * Types of tenant scope for entity filtering.
 */
public enum TenantScopeType {
    
    /**
     * Entity is scoped to a merchant.
     * Filter by merchant_id column.
     */
    MERCHANT,
    
    /**
     * Entity is scoped to an outlet.
     * Filter by outlet_id column (and inherits merchant scope).
     */
    OUTLET,
    
    /**
     * Entity is scoped to a user.
     * Filter by user_id column.
     */
    USER,
    
    /**
     * Entity is global (no tenant filtering).
     * Only SUPER_ADMIN/ADMIN can manage.
     */
    GLOBAL
}
