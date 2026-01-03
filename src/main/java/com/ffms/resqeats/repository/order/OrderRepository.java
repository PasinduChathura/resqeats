package com.ffms.resqeats.repository.order;

import com.ffms.resqeats.enums.order.OrderStatus;
import com.ffms.resqeats.models.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByShopId(Long shopId);

    Page<Order> findByShopId(Long shopId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUserId(@Param("userId") Long userId, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByShopId(@Param("shopId") Long shopId, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.shopAcceptanceDeadline < :now")
    List<Order> findOrdersPendingAcceptanceExpired(@Param("status") OrderStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM Order o WHERE o.status = 'READY_FOR_PICKUP' AND o.pickupDeadline < :now")
    List<Order> findOrdersPickupExpired(@Param("now") LocalDateTime now);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByShopId(@Param("shopId") Long shopId);

    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId AND o.status = 'PENDING_SHOP_ACCEPTANCE' ORDER BY o.createdAt ASC")
    List<Order> findPendingOrdersByShopId(@Param("shopId") Long shopId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = 'PENDING_SHOP_ACCEPTANCE' AND o.createdAt < :expiryTime")
    List<Order> findExpiredPendingOrders(@Param("expiryTime") Date expiryTime);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = 'READY_FOR_PICKUP' AND o.readyAt < :expiryTime")
    List<Order> findUnpickedReadyOrders(@Param("expiryTime") LocalDateTime expiryTime);

    @Query("SELECT AVG(o.userRating) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' AND o.userRating IS NOT NULL")
    Double calculateAverageRatingByShopId(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED' AND o.userRating IS NOT NULL")
    Integer countRatingsByShopId(@Param("shopId") Long shopId);
}
