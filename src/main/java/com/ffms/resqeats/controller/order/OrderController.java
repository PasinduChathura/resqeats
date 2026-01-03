package com.ffms.resqeats.controller.order;

import com.ffms.resqeats.dto.order.*;
import com.ffms.resqeats.enums.order.OrderStatus;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== USER ENDPOINTS ====================

    /**
     * Create a new order from cart
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.createOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.getOrderById(orderId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's orders with pagination
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.getUserOrders(userDetails.getId(), status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get user's active orders
     */
    @GetMapping("/my-orders/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMyActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = orderService.getActiveUserOrders(userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reason = request != null ? request.getReason() : null;
        OrderResponse response = orderService.cancelOrder(orderId, userDetails.getId(), reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Rate an order
     */
    @PostMapping("/{orderId}/rate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> rateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRatingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.rateOrder(orderId, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== SHOP OWNER ENDPOINTS ====================

    /**
     * Get shop orders with pagination
     */
    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Page<OrderResponse>> getShopOrders(
            @PathVariable Long shopId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.getShopOrders(shopId, userDetails.getId(), status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get pending orders for shop
     */
    @GetMapping("/shop/{shopId}/pending")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<List<OrderResponse>> getPendingShopOrders(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = orderService.getPendingShopOrders(shopId, userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Get active orders for shop
     */
    @GetMapping("/shop/{shopId}/active")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<List<OrderResponse>> getActiveShopOrders(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = orderService.getActiveShopOrders(shopId, userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Accept an order
     */
    @PostMapping("/{orderId}/accept")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.acceptOrder(orderId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Decline an order
     */
    @PostMapping("/{orderId}/decline")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<OrderResponse> declineOrder(
            @PathVariable Long orderId,
            @RequestBody OrderActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.declineOrder(orderId, userDetails.getId(), request.getReason());
        return ResponseEntity.ok(response);
    }

    /**
     * Mark order as preparing
     */
    @PostMapping("/{orderId}/preparing")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<OrderResponse> markOrderPreparing(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.markOrderPreparing(orderId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Mark order as ready for pickup
     */
    @PostMapping("/{orderId}/ready")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<OrderResponse> markOrderReady(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.markOrderReady(orderId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Verify pickup with code
     */
    @PostMapping("/{orderId}/pickup")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<OrderResponse> verifyPickup(
            @PathVariable Long orderId,
            @RequestParam String code,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse response = orderService.verifyPickup(orderId, code, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
