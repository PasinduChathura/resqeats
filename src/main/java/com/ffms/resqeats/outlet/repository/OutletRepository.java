package com.ffms.resqeats.outlet.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Outlet repository per SRS Section 6.5.
 * 
 * TENANT SCOPED: 
 * - SUPER_ADMIN/ADMIN: Full access to all outlets
 * - MERCHANT: Access only to their merchant's outlets (Hibernate filter)
 * - OUTLET_USER: Access only to their assigned outlet
 * - USER: Read access to ACTIVE outlets only (public data)
 */
@Repository
public interface OutletRepository extends BaseScopedRepository<Outlet> {

    // ============== MERCHANT-SCOPED METHODS ==============
    // These are automatically filtered by merchantFilter for MERCHANT role
    
    List<Outlet> findByMerchantId(UUID merchantId);

    Page<Outlet> findByMerchantId(UUID merchantId, Pageable pageable);

    @Query("SELECT o FROM Outlet o WHERE o.merchantId = :merchantId AND o.status = :status")
    List<Outlet> findByMerchantIdAndStatus(@Param("merchantId") UUID merchantId, 
                                            @Param("status") OutletStatus status);

    @Query("SELECT COUNT(o) FROM Outlet o WHERE o.merchantId = :merchantId AND o.status = 'ACTIVE'")
    long countActiveOutletsByMerchantId(@Param("merchantId") UUID merchantId);

    // ============== ADMIN-ONLY METHODS ==============
    // These should only be called by ADMIN/SUPER_ADMIN
    
    Page<Outlet> findByStatus(OutletStatus status, Pageable pageable);

    List<Outlet> findByStatusIn(List<OutletStatus> statuses);

    @Query("SELECT COUNT(o) FROM Outlet o WHERE o.status = 'ACTIVE'")
    long countActiveOutlets();

    List<Outlet> findAllByStatus(OutletStatus status);

    // ============== PUBLIC ACCESS METHODS ==============
    // These are for customer-facing features (only ACTIVE outlets)

    /**
     * Find nearby outlets within radius using Haversine formula.
     * Returns ACTIVE outlets sorted by distance (public access).
     */
    @Query(value = """
            SELECT o.*, 
                   (6371 * acos(cos(radians(:lat)) * cos(radians(o.latitude)) * 
                   cos(radians(o.longitude) - radians(:lon)) + 
                   sin(radians(:lat)) * sin(radians(o.latitude)))) AS distance
            FROM outlets o 
            WHERE o.status = 'ACTIVE' 
            AND (6371 * acos(cos(radians(:lat)) * cos(radians(o.latitude)) * 
                 cos(radians(o.longitude) - radians(:lon)) + 
                 sin(radians(:lat)) * sin(radians(o.latitude)))) < :radius
            ORDER BY distance
            """, nativeQuery = true)
    List<Outlet> findNearbyOutlets(@Param("lat") BigDecimal latitude,
                                    @Param("lon") BigDecimal longitude,
                                    @Param("radius") Double radiusKm);

    Page<Outlet> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT o FROM Outlet o WHERE o.name LIKE %:query% OR o.address LIKE %:query%")
    Page<Outlet> searchByNameOrAddress(@Param("query") String query, Pageable pageable);

    // ============== SCOPED UTILITY METHODS ==============

    /**
     * Get current user's accessible outlets based on role.
     * - MERCHANT: All outlets for their merchant
     * - OUTLET_USER: Only their assigned outlet
     */
    default List<Outlet> findAccessibleOutlets() {
        var context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return findAll(); // ADMIN/SUPER_ADMIN see all
        }
        
        if (context.getRole() == UserRole.MERCHANT && context.getMerchantId() != null) {
            return findByMerchantId(context.getMerchantId());
        }
        
        if (context.hasOutletScope() && context.getOutletId() != null) {
            return findById(context.getOutletId())
                    .map(List::of)
                    .orElse(List.of());
        }
        
        // Regular users - return empty (they use findNearbyOutlets for public access)
        return List.of();
    }

    /**
     * Validate scope for Outlet entity.
     */
    @Override
    default void validateScope(Outlet entity) {
        if (entity == null) return;
        requireMerchantScope(entity.getMerchantId());
    }
}
