package com.ffms.resqeats.security.rbac;

import com.ffms.resqeats.exception.security.AccessDeniedException;
import com.ffms.resqeats.exception.security.InsufficientRoleException;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Centralized RBAC (Role-Based Access Control) Policy Engine.
 * 
 * ALL authorization decisions MUST go through this engine.
 * No duplicate security logic allowed in controllers or services.
 * 
 * Rules:
 * - CUSTOMER_USER → blocked from internal APIs
 * - OUTLET_USER → outlet-only access
 * - MERCHANT_USER → merchant-only access
 * - ADMIN → global access (audited)
 * - SUPER_ADMIN → unrestricted (audited)
 */
@Component
@Slf4j
public class RbacPolicyEngine {

    // ========== Role-Based Checks ==========

    /**
     * Require user to be authenticated.
     * @throws AccessDeniedException if not authenticated
     */
    public void requireAuthenticated() {
        if (!SecurityContextHolder.isAuthenticated()) {
            log.warn("Access denied: Authentication required");
            throw new AccessDeniedException("Authentication required");
        }
    }

    /**
     * Require user to have at least the specified role.
     * @throws InsufficientRoleException if role check fails
     */
    public void requireRole(UserRole minimumRole) {
        requireAuthenticated();
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        if (!context.hasRole(minimumRole)) {
            log.warn("Access denied: User {} with role {} attempted action requiring role {}",
                    context.getUserId(), context.getRole(), minimumRole);
            auditAccessDenied(context, "Insufficient role: required " + minimumRole);
            throw new InsufficientRoleException(
                    String.format("Role %s or higher required", minimumRole));
        }
    }

    /**
     * Require SUPER_ADMIN role.
     */
    public void requireSuperAdmin() {
        requireRole(UserRole.SUPER_ADMIN);
    }

    /**
     * Require ADMIN or higher role.
     */
    public void requireAdmin() {
        requireRole(UserRole.ADMIN);
    }

    /**
     * Require MERCHANT_USER or higher role.
     */
    public void requireMerchant() {
        requireRole(UserRole.MERCHANT_USER);
    }

    /**
     * Require OUTLET_USER or higher role.
     */
    public void requireOutletUser() {
        requireRole(UserRole.OUTLET_USER);
    }

    // ========== Scope-Based Checks ==========

    /**
     * Require access to the specified merchant.
     * - SUPER_ADMIN/ADMIN: always allowed
    * - MERCHANT_USER: must match their merchantId
     * - OUTLET_USER: must match their merchant (via outlet)
    * - CUSTOMER_USER: denied
     */
    public void requireMerchantAccess(Long merchantId) {
        requireAuthenticated();
        
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        // SUPER_ADMIN and ADMIN have global access
        if (context.hasGlobalAccess()) {
            auditAdminAccess(context, "merchant", merchantId);
            return;
        }
        
        // MERCHANT_USER must match their own merchant
        if (context.getRole() == UserRole.MERCHANT_USER) {
            if (!merchantId.equals(context.getMerchantId())) {
                log.warn("Access denied: Merchant {} attempted to access merchant {}",
                        context.getMerchantId(), merchantId);
                throw new AccessDeniedException("Access denied to this merchant");
            }
            return;
        }
        
        // OUTLET_USER - check if outlet belongs to merchant
        if (context.getRole() == UserRole.OUTLET_USER) {
            // Outlet users need their merchant ID checked
            if (!merchantId.equals(context.getMerchantId())) {
                log.warn("Access denied: Outlet user {} attempted to access merchant {}",
                        context.getUserId(), merchantId);
                throw new AccessDeniedException("Access denied to this merchant");
            }
            return;
        }
        
        // CUSTOMER_USER role - denied
        log.warn("Access denied: User {} attempted to access merchant APIs", context.getUserId());
        throw new AccessDeniedException("Merchant access denied for regular users");
    }

    /**
     * Require access to the specified outlet.
     * - SUPER_ADMIN/ADMIN: always allowed
    * - MERCHANT_USER: must own the outlet (via merchantId)
     * - OUTLET_USER: must be assigned to the outlet
    * - CUSTOMER_USER: denied
     */
    public void requireOutletAccess(Long outletId, Long outletMerchantId) {
        requireAuthenticated();
        
        if (outletId == null) {
            throw new IllegalArgumentException("Outlet ID cannot be null");
        }
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        // SUPER_ADMIN and ADMIN have global access
        if (context.hasGlobalAccess()) {
            auditAdminAccess(context, "outlet", outletId);
            return;
        }
        
        // MERCHANT_USER must own the outlet
        if (context.getRole() == UserRole.MERCHANT_USER) {
            if (!context.getMerchantId().equals(outletMerchantId)) {
                log.warn("Access denied: Merchant {} attempted to access outlet {} owned by {}",
                        context.getMerchantId(), outletId, outletMerchantId);
                throw new AccessDeniedException("Access denied to this outlet");
            }
            return;
        }
        
        // OUTLET_USER must be assigned to the outlet
        if (context.getRole() == UserRole.OUTLET_USER) {
            if (!outletId.equals(context.getOutletId())) {
                log.warn("Access denied: Outlet user {} attempted to access outlet {}",
                        context.getUserId(), outletId);
                throw new AccessDeniedException("Access denied to this outlet");
            }
            return;
        }
        
        // CUSTOMER_USER role - denied
        log.warn("Access denied: User {} attempted to access outlet APIs", context.getUserId());
        throw new AccessDeniedException("Outlet access denied for regular users");
    }

    /**
     * Require access to the specified user's data.
     * - SUPER_ADMIN/ADMIN: always allowed
     * - Others: can only access their own data
     */
    public void requireUserAccess(Long targetUserId) {
        requireAuthenticated();
        
        if (targetUserId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        // SUPER_ADMIN and ADMIN have global access
        if (context.hasGlobalAccess()) {
            auditAdminAccess(context, "user", targetUserId);
            return;
        }
        
        // Others can only access their own data
        if (!targetUserId.equals(context.getUserId())) {
            log.warn("Access denied: User {} attempted to access user {}'s data",
                    context.getUserId(), targetUserId);
            throw new AccessDeniedException("Access denied to other user's data");
        }
    }

    /**
     * Check if current user owns the specified resource.
     */
    public boolean isResourceOwner(Long resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }
        return resourceOwnerId.equals(SecurityContextHolder.getCurrentUserId());
    }

    /**
     * Check if current user can access the specified merchant.
     */
    public boolean canAccessMerchant(Long merchantId) {
        if (merchantId == null) {
            return false;
        }
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return true;
        }
        
        return merchantId.equals(context.getMerchantId());
    }

    /**
     * Check if current user can access the specified outlet.
     */
    public boolean canAccessOutlet(Long outletId) {
        if (outletId == null) {
            return false;
        }
        
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return true;
        }
        
        if (context.getRole() == UserRole.MERCHANT_USER) {
            // Merchant can access any of their outlets (checked via merchant_id filter)
            return true;
        }
        
        return outletId.equals(context.getOutletId());
    }

    // ========== Audit Logging ==========

    private void auditAdminAccess(ResqeatsSecurityContext context, String resourceType, Long resourceId) {
        if (context.requiresAudit()) {
            log.info("AUDIT: {} {} accessed {} {}",
                    context.getRole(), context.getUserId(), resourceType, resourceId);
        }
    }

    private void auditAccessDenied(ResqeatsSecurityContext context, String reason) {
        log.warn("AUDIT: Access denied for user {} (role: {}). Reason: {}",
                context.getUserId(), context.getRole(), reason);
    }
}
