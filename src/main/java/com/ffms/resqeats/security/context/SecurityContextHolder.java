package com.ffms.resqeats.security.context;

import com.ffms.resqeats.user.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Thread-local holder for the current request's security context.
 * 
 * CRITICAL: This context is set ONCE at request entry by the AuthTokenFilter
 * and cleared ONCE after request completion. It is IMMUTABLE and READ-ONLY
 * throughout the request lifecycle.
 * 
 * Usage:
 * - Controllers and services access via SecurityContextHolder.getContext()
 * - Never modify the context after it's set
 * - Never create new contexts except in the authentication filter
 */
@Component
public class SecurityContextHolder {

    private static final ThreadLocal<ResqeatsSecurityContext> contextHolder = new ThreadLocal<>();

    /**
     * Get the current security context.
     * Returns anonymous context if none is set.
     */
    public static ResqeatsSecurityContext getContext() {
        ResqeatsSecurityContext context = contextHolder.get();
        if (context == null) {
            return ResqeatsSecurityContext.anonymous(UUID.randomUUID().toString());
        }
        return context;
    }

    /**
     * Set the security context for the current thread.
     * ONLY to be called by AuthTokenFilter at request entry.
     */
    public static void setContext(ResqeatsSecurityContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Security context cannot be null");
        }
        contextHolder.set(context);
    }

    /**
     * Clear the security context.
     * ONLY to be called by AuthTokenFilter after request completion.
     */
    public static void clearContext() {
        contextHolder.remove();
    }

    /**
     * Check if a context is currently set.
     */
    public static boolean hasContext() {
        return contextHolder.get() != null;
    }

    // ========== Convenience Methods ==========

    /**
     * Get current user ID.
     */
    public static UUID getCurrentUserId() {
        return getContext().getUserId();
    }

    /**
     * Get current user role.
     */
    public static UserRole getCurrentRole() {
        return getContext().getRole();
    }

    /**
     * Get current merchant ID (for scoping).
     */
    public static UUID getCurrentMerchantId() {
        return getContext().getMerchantId();
    }

    /**
     * Get current outlet ID (for scoping).
     */
    public static UUID getCurrentOutletId() {
        return getContext().getOutletId();
    }

    /**
     * Check if current user is authenticated.
     */
    public static boolean isAuthenticated() {
        return !getContext().isAnonymous();
    }

    /**
     * Check if current user has at least the specified role.
     */
    public static boolean hasRole(UserRole role) {
        return getContext().hasRole(role);
    }

    /**
     * Check if current user is SUPER_ADMIN.
     */
    public static boolean isSuperAdmin() {
        return getContext().isSuperAdmin();
    }

    /**
     * Check if current user is ADMIN or higher.
     */
    public static boolean isAdmin() {
        return getContext().isAdmin();
    }

    /**
     * Get effective merchant ID for data filtering.
     */
    public static UUID getEffectiveMerchantId() {
        return getContext().getEffectiveMerchantId();
    }

    /**
     * Get effective outlet ID for data filtering.
     */
    public static UUID getEffectiveOutletId() {
        return getContext().getEffectiveOutletId();
    }

    /**
     * Get correlation ID for request tracing.
     */
    public static String getCorrelationId() {
        return getContext().getCorrelationId();
    }
}
