package com.ffms.resqeats.repository.food;

import com.ffms.resqeats.models.food.SecretBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecretBoxRepository extends JpaRepository<SecretBox, Long> {

    List<SecretBox> findByShopId(Long shopId);

    Page<SecretBox> findByShopId(Long shopId, Pageable pageable);

    Optional<SecretBox> findByIdAndShopId(Long id, Long shopId);

    @Query("SELECT sb FROM SecretBox sb WHERE sb.shop.id = :shopId AND sb.isActive = true AND sb.isVisible = true AND sb.quantityAvailable > 0")
    List<SecretBox> findAvailableBoxesByShopId(@Param("shopId") Long shopId);

    @Query("SELECT sb FROM SecretBox sb WHERE sb.shop.id = :shopId AND sb.isActive = true AND sb.isVisible = true AND sb.quantityAvailable > 0")
    Page<SecretBox> findAvailableBoxesByShopId(@Param("shopId") Long shopId, Pageable pageable);

    @Query("SELECT sb FROM SecretBox sb WHERE sb.isActive = true AND sb.isVisible = true AND sb.quantityAvailable > 0 AND sb.shop.status = 'APPROVED' AND sb.shop.isOpen = true")
    Page<SecretBox> findAllAvailableBoxes(Pageable pageable);

    @Query(value = """
            SELECT sb.* FROM secret_boxes sb
            INNER JOIN shops s ON sb.shop_id = s.id
            WHERE sb.is_active = true AND sb.is_visible = true AND sb.quantity_available > 0
            AND s.status = 'APPROVED' AND s.is_open = true
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
    List<SecretBox> findNearbyAvailableBoxes(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") double radius
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sb FROM SecretBox sb WHERE sb.id = :id")
    Optional<SecretBox> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT sb FROM SecretBox sb WHERE sb.expiryTime < :now AND sb.isActive = true")
    List<SecretBox> findExpiredBoxes(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(sb) FROM SecretBox sb WHERE sb.shop.id = :shopId AND sb.isActive = true AND sb.quantityAvailable > 0")
    long countAvailableBoxesByShopId(@Param("shopId") Long shopId);
}
