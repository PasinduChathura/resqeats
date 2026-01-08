package com.ffms.resqeats.order.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.enums.OrderStatus;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order repository per SRS Section 6.8.
 * 
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to all orders
 * - MERCHANT: Access to orders from their merchant's outlets
 * - OUTLET_USER: Access only to their outlet's orders (outletFilter)
 * - USER: Access only to their own orders (userFilter)
 */
@Repository
public interface OrderRepository extends BaseScopedRepository<Order>, JpaSpecificationExecutor<Order> {

    // ============== GENERAL LOOKUPS ==============
    
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // ============== USER-SCOPED METHODS ==============
    // These are automatically filtered by userFilter for USER role
    
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUserId(@Param("userId") UUID userId, 
                                          @Param("statuses") List<OrderStatus> statuses);

    // ============== OUTLET-SCOPED METHODS ==============
    // These are automatically filtered by outletFilter for OUTLET_USER role
    
    Page<Order> findByOutletId(UUID outletId, Pageable pageable);

    Page<Order> findByOutletIdAndStatus(UUID outletId, OrderStatus status, Pageable pageable);

    List<Order> findByOutletIdAndStatusIn(UUID outletId, List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.outletId = :outletId AND o.status IN :statuses " +
           "AND o.createdAt >= :since")
    long countByOutletIdAndStatusesSince(@Param("outletId") UUID outletId,
                                          @Param("statuses") List<OrderStatus> statuses,
                                          @Param("since") LocalDateTime since);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.outletId = :outletId AND o.status = 'COMPLETED' " +
           "AND o.completedAt >= :since")
    java.math.BigDecimal sumRevenueByOutletIdSince(@Param("outletId") UUID outletId,
                                                    @Param("since") LocalDateTime since);

    @Query("SELECT o FROM Order o WHERE o.outletId IN :outletIds ORDER BY o.createdAt DESC")
    Page<Order> findByOutletIds(@Param("outletIds") List<UUID> outletIds, Pageable pageable);

    // ============== SYSTEM/SCHEDULED TASK METHODS ==============
    // These bypass tenant filtering for system operations

    /**
     * Find orders pending acceptance that have exceeded their deadline.
     * Used for auto-expiration per BR-003. SYSTEM USE ONLY.
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING_OUTLET_ACCEPTANCE' " +
           "AND o.acceptanceDeadline < :now")
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);

    /**
     * Find orders ready for pickup that have exceeded their pickup deadline.
     * Used for auto-expiration per BR-007. SYSTEM USE ONLY.
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'READY_FOR_PICKUP' " +
           "AND o.pickupBy < :now")
    List<Order> findExpiredPickupOrders(@Param("now") LocalDateTime now);

    /**
     * Find picked up orders ready to be completed.
     * SYSTEM USE ONLY.
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PICKED_UP' " +
           "AND o.pickedUpAt < :cutoff")
    List<Order> findPickedUpOrdersReadyForCompletion(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    long countOrdersSince(@Param("since") LocalDateTime since);

    // ============== SCOPED ACCESS METHODS ==============

    /**
     * Get current user's orders based on role.
     * - USER: Their own orders
     * - OUTLET_USER: Their outlet's orders
     * - MERCHANT: All orders from their outlets
     */
    default Page<Order> findMyOrders(Pageable pageable) {
        var context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return findAll(pageable);
        }
        
        if (context.getRole() == UserRole.USER && context.getUserId() != null) {
            return findByUserId(context.getUserId(), pageable);
        }
        
        if (context.hasOutletScope() && context.getOutletId() != null) {
            return findByOutletId(context.getOutletId(), pageable);
        }
        
        // Return empty page for unauthorized access
        return Page.empty(pageable);
    }

    /**
     * Validate scope for Order entity.
     */
    @Override
    default void validateScope(Order entity) {
        if (entity == null) return;
        
        var context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) return;
        
        // OUTLET_USER can access orders for their outlet
        if (context.hasOutletScope()) {
            requireOutletScope(entity.getOutletId());
            return;
        }
        
        // Regular USER can only access their own orders
        requireUserScope(entity.getUserId());
    }
}
