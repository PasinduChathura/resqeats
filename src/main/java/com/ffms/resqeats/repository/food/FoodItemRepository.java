package com.ffms.resqeats.repository.food;

import com.ffms.resqeats.enums.food.FoodCategory;
import com.ffms.resqeats.models.food.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByShopId(Long shopId);

    Page<FoodItem> findByShopId(Long shopId, Pageable pageable);

    Optional<FoodItem> findByIdAndShopId(Long id, Long shopId);

    List<FoodItem> findByShopIdAndIsActiveTrue(Long shopId);

    Page<FoodItem> findByShopIdAndCategory(Long shopId, FoodCategory category, Pageable pageable);

    @Query("SELECT f FROM FoodItem f WHERE f.shop.id = :shopId AND f.isActive = true AND f.expiryDate > :now")
    List<FoodItem> findActiveItemsByShopId(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);

    @Query("SELECT f FROM FoodItem f WHERE f.expiryDate < :date AND f.isActive = true")
    List<FoodItem> findExpiredItems(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(f) FROM FoodItem f WHERE f.shop.id = :shopId AND f.isActive = true")
    long countActiveItemsByShopId(@Param("shopId") Long shopId);
}
