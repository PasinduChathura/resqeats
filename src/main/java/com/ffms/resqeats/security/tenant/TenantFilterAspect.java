package com.ffms.resqeats.security.tenant;

import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Hibernate Filter Configuration for Multi-Tenant Scope Enforcement.
 * 
 * This aspect automatically enables tenant filters before repository operations
 * based on the current security context.
 * 
 * Filter Rules:
 * - SUPER_ADMIN → No filter applied
 * - ADMIN → No filter applied (audited)
 * - MERCHANT → merchant_id filter enabled for outlets, items, orders; owner filter for merchants
 * - OUTLET_USER → outlet_id filter enabled (and merchant_id)
 * - USER → user_id filter for owned resources (orders, notifications)
 * 
 * ALL database queries are automatically filtered at the persistence layer.
 * This is the PRIMARY enforcement mechanism for tenant isolation.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterAspect {

    private final EntityManager entityManager;

    // ===== MERCHANT SCOPED FILTERS =====
    public static final String OUTLET_MERCHANT_FILTER = "outletMerchantFilter";
    public static final String ITEM_MERCHANT_FILTER = "itemMerchantFilter";
    public static final String MERCHANT_OWNER_FILTER = "merchantOwnerFilter";
    
    // ===== OUTLET SCOPED FILTERS =====
    public static final String ORDER_OUTLET_FILTER = "orderOutletFilter";
    public static final String OUTLET_ITEM_OUTLET_FILTER = "outletItemOutletFilter";
    public static final String OUTLET_HOURS_OUTLET_FILTER = "outletHoursOutletFilter";
    
    // ===== USER SCOPED FILTERS =====
    public static final String ORDER_USER_FILTER = "orderUserFilter";
    public static final String NOTIFICATION_USER_FILTER = "notificationUserFilter";
    public static final String PAYMENT_METHOD_USER_FILTER = "paymentMethodUserFilter";
    public static final String REFRESH_TOKEN_USER_FILTER = "refreshTokenUserFilter";
    
    // ===== USER-MERCHANT SCOPED FILTERS =====
    public static final String USER_MERCHANT_FILTER = "userMerchantFilter";
    public static final String USER_OUTLET_FILTER = "userOutletFilter";
    
    // ===== CHILD ENTITY FILTERS (parent-scoped) =====
    public static final String ORDER_ITEM_ORDER_FILTER = "orderItemOrderFilter";
    public static final String PAYMENT_ORDER_FILTER = "paymentOrderFilter";

    /**
     * Enable tenant filters before any repository method execution.
     */
    @Around("execution(* com.ffms.resqeats..repository..*Repository.*(..))")
    public Object enableTenantFilters(ProceedingJoinPoint joinPoint) throws Throwable {
        Session session = entityManager.unwrap(Session.class);
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        try {
            applyFilters(session, context);
            return joinPoint.proceed();
        } finally {
            // Filters are disabled automatically when session closes
            // but we disable explicitly for safety
            disableFilters(session);
        }
    }

    /**
     * Apply appropriate filters based on user role and scope.
     */
    private void applyFilters(Session session, ResqeatsSecurityContext context) {
        if (context.isAnonymous()) {
            // Anonymous users - no tenant context, public data only
            log.trace("Anonymous request - no tenant filters applied");
            return;
        }

        // SUPER_ADMIN and ADMIN have global access - no filters
        if (context.hasGlobalAccess()) {
            log.trace("Admin access - no tenant filters applied. User: {}", context.getUserId());
            if (context.requiresAudit()) {
                log.info("AUDIT: Global data access by {} ({})", 
                        context.getUserId(), context.getRole());
            }
            return;
        }

        UserRole role = context.getRole();
        UUID userId = context.getUserId();
        
        // MERCHANT - filter by merchant_id for outlets/items, owner for merchants
        if (role == UserRole.MERCHANT && context.getMerchantId() != null) {
            UUID merchantId = context.getMerchantId();
            
            // Outlet filter - see only merchant's outlets
            enableFilter(session, OUTLET_MERCHANT_FILTER, "merchantId", merchantId.toString());
            
            // Item filter - see only merchant's items
            enableFilter(session, ITEM_MERCHANT_FILTER, "merchantId", merchantId.toString());
            
            // User filter - see only users within merchant
            enableFilter(session, USER_MERCHANT_FILTER, "merchantId", merchantId.toString());
            
            // Merchant filter - see only owned merchant
            if (userId != null) {
                enableFilter(session, MERCHANT_OWNER_FILTER, "ownerUserId", userId.toString());
            }
            
            log.trace("Merchant filters enabled. MerchantId: {}, UserId: {}", merchantId, userId);
        }

        // OUTLET_USER - filter by outlet_id
        if (role == UserRole.OUTLET_USER && context.hasOutletScope()) {
            UUID outletId = context.getOutletId();
            UUID merchantId = context.getMerchantId();
            
            // Order filter - see only outlet's orders
            enableFilter(session, ORDER_OUTLET_FILTER, "outletId", outletId.toString());
            
            // OutletItem filter - see only outlet's items
            enableFilter(session, OUTLET_ITEM_OUTLET_FILTER, "outletId", outletId.toString());
            
            // OutletHours filter - see only outlet's hours
            enableFilter(session, OUTLET_HOURS_OUTLET_FILTER, "outletId", outletId.toString());
            
            // User filter - see only users within outlet
            enableFilter(session, USER_OUTLET_FILTER, "outletId", outletId.toString());
            
            // Also apply merchant filter for outlets/items if merchant context exists
            if (merchantId != null) {
                enableFilter(session, OUTLET_MERCHANT_FILTER, "merchantId", merchantId.toString());
                enableFilter(session, ITEM_MERCHANT_FILTER, "merchantId", merchantId.toString());
            }
            
            log.trace("Outlet filters enabled. OutletId: {}", outletId);
        }

        // USER (regular customer) - filter by user_id for owned resources
        if (role == UserRole.USER && userId != null) {
            // Order filter - see only own orders
            enableFilter(session, ORDER_USER_FILTER, "userId", userId.toString());
            
            // Notification filter - see only own notifications
            enableFilter(session, NOTIFICATION_USER_FILTER, "userId", userId.toString());
            
            // PaymentMethod filter - see only own payment methods
            enableFilter(session, PAYMENT_METHOD_USER_FILTER, "userId", userId.toString());
            
            // RefreshToken filter - see only own tokens
            enableFilter(session, REFRESH_TOKEN_USER_FILTER, "userId", userId.toString());
            
            log.trace("User filters enabled. UserId: {}", userId);
        }
    }

    /**
     * Helper to enable a filter with parameter.
     */
    private void enableFilter(Session session, String filterName, String paramName, String paramValue) {
        try {
            Filter filter = session.enableFilter(filterName);
            filter.setParameter(paramName, paramValue);
        } catch (Exception e) {
            log.trace("Filter {} not applicable for current entity", filterName);
        }
    }

    /**
     * Disable all tenant filters.
     */
    private void disableFilters(Session session) {
        String[] filters = {
            // Merchant scoped
            OUTLET_MERCHANT_FILTER,
            ITEM_MERCHANT_FILTER,
            MERCHANT_OWNER_FILTER,
            // Outlet scoped
            ORDER_OUTLET_FILTER,
            OUTLET_ITEM_OUTLET_FILTER,
            OUTLET_HOURS_OUTLET_FILTER,
            // User scoped
            ORDER_USER_FILTER,
            NOTIFICATION_USER_FILTER,
            PAYMENT_METHOD_USER_FILTER,
            REFRESH_TOKEN_USER_FILTER,
            // User-merchant scoped
            USER_MERCHANT_FILTER,
            USER_OUTLET_FILTER,
            // Child entity filters
            ORDER_ITEM_ORDER_FILTER,
            PAYMENT_ORDER_FILTER
        };
        
        for (String filterName : filters) {
            try {
                session.disableFilter(filterName);
            } catch (Exception ignored) {}
        }
    }
}
