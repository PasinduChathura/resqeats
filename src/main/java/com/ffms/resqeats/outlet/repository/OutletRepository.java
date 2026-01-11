package com.ffms.resqeats.outlet.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Outlet repository per SRS Section 6.5.
 * <p>
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to all outlets
 * - MERCHANT_USER: Access only to their merchant's outlets
 * - OUTLET_USER: Access only to their assigned outlet
 * - CUSTOMER_USER: Read access to ACTIVE outlets only (public data)
 * <p>
 * Note: All methods leverage JpaSpecificationExecutor for dynamic filtering.
 * Use OutletSpecification for complex queries with proper scope handling.
 */
@Repository
public interface OutletRepository extends BaseScopedRepository<Outlet>, JpaSpecificationExecutor<Outlet> {

    // ============== MERCHANT-SCOPED METHODS ==============

    /**
     * Find all outlets for a specific merchant.
     * Used for merchant dropdown lookups.
     */
    List<Outlet> findByMerchantId(Long merchantId);

    /**
     * Find all outlets for a specific merchant with pagination.
     * Primary method for merchant outlet listing.
     */
    Page<Outlet> findByMerchantId(Long merchantId, Pageable pageable);

    // ============== SEARCH METHODS ==============

    /**
     * Search outlets by merchant and name query.
     * Used for admin lookup and search functionality.
     */
    @Query("""
            SELECT o FROM Outlet o
            WHERE (:merchantId IS NULL OR o.merchantId = :merchantId)
                AND (:query IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    List<Outlet> searchOutlets(
            @Param("merchantId") Long merchantId,
            @Param("query") String query
    );

    /**
     * Search outlets by merchant ID and name (case-insensitive).
     * Used for merchant-scoped outlet lookup.
     */
    List<Outlet> findByMerchantIdAndNameContainingIgnoreCase(Long merchantId, String name);

    // ============== GEO SERVICE METHODS ==============
    // These methods are used internally by GeoService for geo-spatial queries

    /**
     * Find outlets by status with pagination.
     * Used by GeoService for paginated geo queries.
     */
    Page<Outlet> findByStatus(OutletStatus status, Pageable pageable);

    /**
     * Find all outlets by status.
     * Used by GeoService for fallback geo queries when Redis is unavailable.
     */
    List<Outlet> findAllByStatus(OutletStatus status);

    // ============== SCOPE VALIDATION ==============

    /**
     * Validates that the current user has access to the outlet's merchant scope.
     */
    @Override
    default void validateScope(Outlet entity) {
        if (entity == null) return;
        requireMerchantScope(entity.getMerchantId());
    }
}
