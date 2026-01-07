package com.ffms.resqeats.security.tenant;

import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
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
 * - MERCHANT → merchant_id filter enabled
 * - OUTLET_USER → outlet_id filter enabled (and merchant_id)
 * - USER → user_id filter for owned resources
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

    public static final String OUTLET_MERCHANT_FILTER = "outletMerchantFilter";
    public static final String ITEM_MERCHANT_FILTER = "itemMerchantFilter";
    public static final String ORDER_OUTLET_FILTER = "orderOutletFilter";
    public static final String OUTLET_ITEM_OUTLET_FILTER = "outletItemOutletFilter";
    public static final String ORDER_USER_FILTER = "orderUserFilter";
    public static final String NOTIFICATION_USER_FILTER = "notificationUserFilter";

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

        // MERCHANT - filter by merchant_id
        if (context.getRole() != null && context.getMerchantId() != null) {
            UUID merchantId = context.getMerchantId();
            if (merchantId != null) {
                Filter outletMerchantFilter = session.enableFilter(OUTLET_MERCHANT_FILTER);
                outletMerchantFilter.setParameter("merchantId", merchantId.toString());
                
                Filter itemMerchantFilter = session.enableFilter(ITEM_MERCHANT_FILTER);
                itemMerchantFilter.setParameter("merchantId", merchantId.toString());
                
                log.trace("Merchant filters enabled. MerchantId: {}", merchantId);
            }
        }

        // OUTLET_USER - filter by outlet_id
        if (context.hasOutletScope()) {
            UUID outletId = context.getOutletId();
            if (outletId != null) {
                Filter orderOutletFilter = session.enableFilter(ORDER_OUTLET_FILTER);
                orderOutletFilter.setParameter("outletId", outletId.toString());
                
                Filter outletItemOutletFilter = session.enableFilter(OUTLET_ITEM_OUTLET_FILTER);
                outletItemOutletFilter.setParameter("outletId", outletId.toString());
                
                log.trace("Outlet filters enabled. OutletId: {}", outletId);
            }
        }

        // USER - filter by user_id for owned resources
        if (context.getUserId() != null) {
            Filter orderUserFilter = session.enableFilter(ORDER_USER_FILTER);
            orderUserFilter.setParameter("userId", context.getUserId().toString());
            
            Filter notificationUserFilter = session.enableFilter(NOTIFICATION_USER_FILTER);
            notificationUserFilter.setParameter("userId", context.getUserId().toString());
            
            log.trace("User filters enabled. UserId: {}", context.getUserId());
        }
    }

    /**
     * Disable all tenant filters.
     */
    private void disableFilters(Session session) {
        try {
            session.disableFilter(OUTLET_MERCHANT_FILTER);
        } catch (Exception ignored) {}
        try {
            session.disableFilter(ITEM_MERCHANT_FILTER);
        } catch (Exception ignored) {}
        try {
            session.disableFilter(ORDER_OUTLET_FILTER);
        } catch (Exception ignored) {}
        try {
            session.disableFilter(OUTLET_ITEM_OUTLET_FILTER);
        } catch (Exception ignored) {}
        try {
            session.disableFilter(ORDER_USER_FILTER);
        } catch (Exception ignored) {}
        try {
            session.disableFilter(NOTIFICATION_USER_FILTER);
        } catch (Exception ignored) {}
    }
}
