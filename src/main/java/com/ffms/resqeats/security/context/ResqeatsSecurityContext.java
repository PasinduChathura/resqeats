package com.ffms.resqeats.security.context;

import com.ffms.resqeats.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Immutable security context for the current request.
 * Populated once at request entry, cleared after request completion.
 * 
 * This is the SINGLE source of truth for security information in the application.
 * Controllers and services MUST NOT parse JWT directly - use this context instead.
 */
@Getter
@Builder
public final class ResqeatsSecurityContext {

    /**
     * Unique identifier for the authenticated user.
     */
    private final UUID userId;

    /**
     * User's role for RBAC enforcement.
     */
    private final UserRole role;

    /**
     * Merchant ID for tenant scoping (nullable for ADMIN/SUPER_ADMIN/USER).
     */
    private final UUID merchantId;

    /**
     * Outlet ID for tenant scoping (nullable for non-OUTLET_USER).
     */
    private final UUID outletId;

    /**
     * User's email for audit logging.
     */
    private final String email;

    /**
     * JWT token ID (jti) for replay protection.
     */
    private final String tokenId;

    /**
     * Token issued at timestamp for security validation.
     */
    private final long issuedAt;

    /**
     * Token expiration timestamp.
     */
    private final long expiresAt;

    /**
     * Whether this is an anonymous/unauthenticated context.
     */
    private final boolean anonymous;

    /**
     * Request correlation ID for tracing.
     */
    private final String correlationId;

    // ========== RBAC Helper Methods ==========

    /**
     * Check if user is SUPER_ADMIN.
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    /**
     * Check if user is ADMIN or higher.
     */
    public boolean isAdmin() {
        return role != null && role.isAtLeast(UserRole.ADMIN);
    }

    /**
     * Check if user is MERCHANT or higher.
     */
    public boolean isMerchant() {
        return role != null && role.isAtLeast(UserRole.MERCHANT);
    }

    /**
     * Check if user is OUTLET_USER or higher.
     */
    public boolean isOutletUser() {
        return role != null && role.isAtLeast(UserRole.OUTLET_USER);
    }

    /**
     * Check if user has at least the specified role.
     */
    public boolean hasRole(UserRole minimumRole) {
        return role != null && role.isAtLeast(minimumRole);
    }

    /**
     * Check if user has global access (no tenant filtering).
     */
    public boolean hasGlobalAccess() {
        return role != null && role.hasGlobalAccess();
    }

    /**
     * Check if user actions require audit logging.
     */
    public boolean requiresAudit() {
        return role != null && role.requiresAudit();
    }

    // ========== Tenant Scope Helper Methods ==========

    /**
     * Check if user has merchant-level scope restriction.
     */
    public boolean hasMerchantScope() {
        return role == UserRole.MERCHANT && merchantId != null;
    }

    /**
     * Check if user has outlet-level scope restriction.
     */
    public boolean hasOutletScope() {
        return role == UserRole.OUTLET_USER && outletId != null;
    }

    /**
     * Get the effective merchant ID for filtering.
     * Returns null for users with global access.
     */
    public UUID getEffectiveMerchantId() {
        if (hasGlobalAccess()) {
            return null; // No filtering for SUPER_ADMIN/ADMIN
        }
        return merchantId;
    }

    /**
     * Get the effective outlet ID for filtering.
     * Returns null for users not restricted to a specific outlet.
     */
    public UUID getEffectiveOutletId() {
        if (hasGlobalAccess() || role == UserRole.MERCHANT) {
            return null; // No outlet filtering
        }
        return outletId;
    }

    // ========== Factory Methods ==========

    /**
     * Create an anonymous security context for unauthenticated requests.
     */
    public static ResqeatsSecurityContext anonymous(String correlationId) {
        return ResqeatsSecurityContext.builder()
                .anonymous(true)
                .correlationId(correlationId)
                .build();
    }

    /**
     * Create a system context for internal operations.
     */
    public static ResqeatsSecurityContext system() {
        return ResqeatsSecurityContext.builder()
                .role(UserRole.SUPER_ADMIN)
                .email("system@resqeats.internal")
                .anonymous(false)
                .correlationId("SYSTEM")
                .build();
    }

    @Override
    public String toString() {
        return String.format("SecurityContext[userId=%s, role=%s, merchantId=%s, outletId=%s]",
                userId, role, merchantId, outletId);
    }
}
