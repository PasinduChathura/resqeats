package com.ffms.resqeats.repository.shop;

import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import com.ffms.resqeats.models.shop.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByIdAndOwnerId(Long id, Long ownerId);

    List<Shop> findByOwnerId(Long ownerId);

    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    Page<Shop> findByCategory(ShopCategory category, Pageable pageable);

    Page<Shop> findByStatusAndCategory(ShopStatus status, ShopCategory category, Pageable pageable);

    @Query("SELECT s FROM Shop s WHERE s.status = :status AND s.isOpen = true")
    Page<Shop> findOpenShops(@Param("status") ShopStatus status, Pageable pageable);

    /**
     * Find shops within a radius using Haversine formula
     * Returns shops within the specified radius (in kilometers) of the given coordinates
     */
    @Query(value = """
            SELECT s.* FROM shops s
            WHERE s.status = 'APPROVED'
            AND (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) <= :radius
            ORDER BY (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) ASC
            """, nativeQuery = true)
    List<Shop> findNearbyShops(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") double radius
    );

    /**
     * Find shops within a radius with pagination
     */
    @Query(value = """
            SELECT s.* FROM shops s
            WHERE s.status = 'APPROVED'
            AND (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) <= :radius
            ORDER BY (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) ASC
            """,
            countQuery = """
                    SELECT COUNT(*) FROM shops s
                    WHERE s.status = 'APPROVED'
                    AND (
                        6371 * acos(
                            cos(radians(:latitude)) * cos(radians(s.latitude)) *
                            cos(radians(s.longitude) - radians(:longitude)) +
                            sin(radians(:latitude)) * sin(radians(s.latitude))
                        )
                    ) <= :radius
                    """,
            nativeQuery = true)
    Page<Shop> findNearbyShopsWithPagination(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") double radius,
            Pageable pageable
    );

    /**
     * Find nearby shops filtered by category
     */
    @Query(value = """
            SELECT s.* FROM shops s
            WHERE s.status = 'APPROVED'
            AND s.category = :category
            AND (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) <= :radius
            ORDER BY (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:longitude)) +
                    sin(radians(:latitude)) * sin(radians(s.latitude))
                )
            ) ASC
            """, nativeQuery = true)
    List<Shop> findNearbyShopsByCategory(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") double radius,
            @Param("category") String category
    );

    @Query("SELECT COUNT(s) FROM Shop s WHERE s.status = :status")
    long countByStatus(@Param("status") ShopStatus status);

    boolean existsByOwnerIdAndStatus(Long ownerId, ShopStatus status);
}
