package com.ffms.resqeats.service.order;

import com.ffms.resqeats.dto.order.*;
import com.ffms.resqeats.enums.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    // User operations
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    OrderResponse getOrderById(Long orderId, Long userId);
    OrderResponse getOrderByNumber(String orderNumber, Long userId);
    Page<OrderResponse> getUserOrders(Long userId, OrderStatus status, Pageable pageable);
    List<OrderResponse> getActiveUserOrders(Long userId);
    OrderResponse cancelOrder(Long orderId, Long userId, String reason);
    OrderResponse rateOrder(Long orderId, Long userId, OrderRatingRequest request);

    // Shop owner operations
    Page<OrderResponse> getShopOrders(Long shopId, Long ownerId, OrderStatus status, Pageable pageable);
    List<OrderResponse> getPendingShopOrders(Long shopId, Long ownerId);
    List<OrderResponse> getActiveShopOrders(Long shopId, Long ownerId);
    OrderResponse acceptOrder(Long orderId, Long ownerId);
    OrderResponse declineOrder(Long orderId, Long ownerId, String reason);
    OrderResponse markOrderPreparing(Long orderId, Long ownerId);
    OrderResponse markOrderReady(Long orderId, Long ownerId);
    OrderResponse verifyPickup(Long orderId, String pickupCode, Long ownerId);

    // System operations
    void processExpiredPendingOrders();
    void processExpiredPickupOrders();
}
