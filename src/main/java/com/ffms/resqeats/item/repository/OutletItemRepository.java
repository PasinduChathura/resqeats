package com.ffms.resqeats.item.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.item.entity.OutletItem;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OutletItem repository for managing item-outlet assignments.
 * 
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to all outlet items
 * - MERCHANT: Access to outlet items for their merchant's outlets
 * - OUTLET_USER: Access only to their outlet's items (outletFilter)
 * - USER: Read access to available items (public data)
 */
@Repository
public interface OutletItemRepository extends BaseScopedRepository<OutletItem> {

    // ============== OUTLET-SCOPED METHODS ==============
    // These are automatically filtered by outletFilter for OUTLET_USER role
    
    List<OutletItem> findByOutletId(Long outletId);

    Optional<OutletItem> findByOutletIdAndItemId(Long outletId, Long itemId);

    @Query("SELECT oi FROM OutletItem oi WHERE oi.outletId = :outletId AND oi.isAvailable = true AND oi.currentQuantity > 0")
    List<OutletItem> findAvailableByOutletId(@Param("outletId") Long outletId);

    @Query("SELECT oi FROM OutletItem oi WHERE oi.outletId = :outletId AND oi.isAvailable = true")
    List<OutletItem> findByOutletIdAndIsAvailableTrue(@Param("outletId") Long outletId);

    boolean existsByOutletIdAndItemId(Long outletId, Long itemId);

    // ============== ITEM-SCOPED METHODS ==============
    
    List<OutletItem> findByItemId(Long itemId);

    Optional<OutletItem> findFirstByItemIdAndIsAvailableTrue(Long itemId);

    // ============== PUBLIC ACCESS METHODS ==============
    // For customer-facing features
    
    @Query("SELECT oi FROM OutletItem oi WHERE oi.outletId IN :outletIds AND oi.isAvailable = true AND oi.currentQuantity > 0")
    List<OutletItem> findAvailableByOutletIds(@Param("outletIds") List<Long> outletIds);

    // ============== MODIFYING OPERATIONS ==============
    // These require proper scope validation at service layer
    
    @Modifying
    @Query("UPDATE OutletItem oi SET oi.currentQuantity = oi.dailyQuantity WHERE oi.outletId = :outletId")
    void resetDailyQuantities(@Param("outletId") Long outletId);

    @Modifying
    @Query("UPDATE OutletItem oi SET oi.currentQuantity = oi.currentQuantity - :quantity WHERE oi.id = :id AND oi.currentQuantity >= :quantity")
    int decrementQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE OutletItem oi SET oi.currentQuantity = oi.currentQuantity + :quantity WHERE oi.id = :id")
    int incrementQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("DELETE FROM OutletItem oi WHERE oi.outletId = :outletId")
    void deleteByOutletId(@Param("outletId") Long outletId);

    @Modifying
    @Query("DELETE FROM OutletItem oi WHERE oi.itemId = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);

    // ============== SCOPED ACCESS METHODS ==============

    /**
     * Get current user's accessible outlet items based on role.
     */
    default List<OutletItem> findAccessibleOutletItems() {
        var context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return findAll();
        }
        
        if (context.hasOutletScope() && context.getOutletId() != null) {
            return findByOutletId(context.getOutletId());
        }
        
        return List.of();
    }

    /**
     * Validate scope for OutletItem entity.
     */
    @Override
    default void validateScope(OutletItem entity) {
        if (entity == null) return;
        requireOutletScope(entity.getOutletId());
    }
}