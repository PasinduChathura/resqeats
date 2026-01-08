package com.ffms.resqeats.item.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.item.entity.Item;
import com.ffms.resqeats.item.enums.ItemCategory;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Item repository per SRS Section 6.6.
 * 
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to all items
 * - MERCHANT: Access only to their merchant's items (merchantFilter)
 * - OUTLET_USER: Read access to items assigned to their outlet
 * - USER: Read access to ACTIVE items only (public data)
 */
@Repository
public interface ItemRepository extends BaseScopedRepository<Item>, JpaSpecificationExecutor<Item> {

    // ============== MERCHANT-SCOPED METHODS ==============
    // These are automatically filtered by merchantFilter for MERCHANT role
    
    List<Item> findByMerchantId(UUID merchantId);

    Page<Item> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<Item> findByMerchantIdAndStatus(UUID merchantId, ItemStatus status, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.merchantId = :merchantId AND " +
           "(i.name LIKE %:query% OR i.description LIKE %:query%)")
    Page<Item> searchByMerchantIdAndQuery(@Param("merchantId") UUID merchantId,
                                          @Param("query") String query,
                                          Pageable pageable);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.merchantId = :merchantId AND i.status = 'ACTIVE'")
    long countActiveByMerchantId(@Param("merchantId") UUID merchantId);

    // ============== ADMIN/PUBLIC ACCESS METHODS ==============
    
    Page<Item> findByStatus(ItemStatus status, Pageable pageable);

    Page<Item> findByCategory(ItemCategory category, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.name LIKE %:query% OR i.description LIKE %:query%")
    Page<Item> searchByQuery(@Param("query") String query, Pageable pageable);

    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ============== SCOPED ACCESS METHODS ==============

    /**
     * Get current user's accessible items based on role.
     * - MERCHANT: All items for their merchant
     * - OUTLET_USER: Items assigned to their outlet (via OutletItem)
     */
    default List<Item> findAccessibleItems() {
        var context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return findAll();
        }
        
        if (context.getMerchantId() != null) {
            return findByMerchantId(context.getMerchantId());
        }
        
        // For other roles, return empty (they use specific queries)
        return List.of();
    }

    /**
     * Validate scope for Item entity.
     */
    @Override
    default void validateScope(Item entity) {
        if (entity == null) return;
        requireMerchantScope(entity.getMerchantId());
    }
}
