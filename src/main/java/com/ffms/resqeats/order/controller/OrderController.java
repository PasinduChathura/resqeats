package com.ffms.resqeats.order.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.order.dto.CreateOrderRequest;
import com.ffms.resqeats.order.dto.OrderDto;
import com.ffms.resqeats.order.dto.OrderFilterDto;
import com.ffms.resqeats.order.dto.OrderListResponseDto;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.service.OrderService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Order controller per SRS Section 6.2.
 * 
 * All endpoints use unified paths - scope filtering is applied automatically
 * at the repository level based on the authenticated user's role and context.
 *
 * Endpoints:
 * POST /orders - Create order (checkout)
 * POST /orders/{orderId}/submit - Submit order with payment
 * GET /orders - List orders (filtered by user's scope)
 * GET /orders/{orderId} - Get order details
 * POST /orders/{orderId}/cancel - Cancel order (customer)
 * POST /orders/{orderId}/accept - Accept order (merchant/outlet)
 * POST /orders/{orderId}/decline - Decline order (merchant/outlet)
 * POST /orders/{orderId}/preparing - Start preparing (merchant/outlet)
 * POST /orders/{orderId}/ready - Mark ready (merchant/outlet)
 * POST /orders/{orderId}/verify - Verify pickup code (merchant/outlet)
 * POST /orders/{orderId}/complete - Complete order (merchant/outlet)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    // =====================
    // Customer Endpoints
    // =====================

    @PostMapping("/orders")
    @Operation(summary = "Create order (checkout)")
    @PreAuthorize("hasRole('CUSTOMER_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order created successfully"));
    }

    @PostMapping("/orders/{orderId}/submit")
    @Operation(summary = "Submit order with payment")
    @PreAuthorize("hasRole('CUSTOMER_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> submitOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody SubmitOrderRequest request) {
        Order order = orderService.submitOrder(orderId, request.getPaymentMethodId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order submitted"));
    }

    @GetMapping("/orders")
    @Operation(summary = "List orders with filters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponseDto>>> getOrders(
            OrderFilterDto filter,
            Pageable pageable) {
        Page<Order> orders = orderService.getAllOrders(filter, pageable);
        PageResponse<OrderListResponseDto> response = PageResponse.from(orders.map(this::toListDto));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get order details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(toDto(order)));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    @PreAuthorize("hasRole('CUSTOMER_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.getReason() : "Customer requested cancellation";
        Order order = orderService.cancelOrder(orderId, reason, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order cancelled"));
    }

    // =====================
    // Order Action Endpoints
    // =====================

    @PostMapping("/orders/{orderId}/accept")
    @Operation(summary = "Accept order")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> acceptOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId,
            @RequestBody(required = false) AcceptOrderRequest request) {
        Order order = orderService.acceptOrder(orderId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order accepted"));
    }

    @PostMapping("/orders/{orderId}/decline")
    @Operation(summary = "Decline order")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> declineOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody DeclineOrderRequest request) {
        Order order = orderService.declineOrder(orderId, request.getReason(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order declined"));
    }

    @PostMapping("/orders/{orderId}/preparing")
    @Operation(summary = "Start preparing order")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> startPreparing(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId) {
        Order order = orderService.startPreparing(orderId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order preparation started"));
    }

    @PostMapping("/orders/{orderId}/ready")
    @Operation(summary = "Mark order ready for pickup")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> markReady(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId) {
        Order order = orderService.markReady(orderId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order marked as ready"));
    }

    @PostMapping("/orders/{orderId}/verify")
    @Operation(summary = "Verify pickup code")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPickupCode(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody VerifyPickupRequest request) {
        Order order = orderService.verifyPickup(orderId, request.getPickupCode(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Pickup verified"));
    }

    @PostMapping("/orders/{orderId}/complete")
    @Operation(summary = "Complete order")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> completeOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId) {
        Order order = orderService.completeOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order completed"));
    }

    // Request/Response DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitOrderRequest {
        private Long paymentMethodId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelOrderRequest {
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcceptOrderRequest {
        private Integer estimatedPrepMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeclineOrderRequest {
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyPickupRequest {
        private String pickupCode;
    }

    // Mapping method
    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .outletId(order.getOutletId())
                .status(order.getStatus())
                .statusDisplay(order.getStatus().name().replace("_", " "))
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .total(order.getTotal())
                .pickupCode(order.getPickupCode())
                .pickupBy(order.getPickupBy())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .acceptedAt(order.getAcceptedAt())
                .readyAt(order.getReadyAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

    // List response DTO mapping method
    private OrderListResponseDto toListDto(Order order) {
        return OrderListResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .outletId(order.getOutletId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .total(order.getTotal())
                .pickupCode(order.getPickupCode())
                .pickupBy(order.getPickupBy())
                .createdAt(order.getCreatedAt())
                .acceptedAt(order.getAcceptedAt())
                .readyAt(order.getReadyAt())
                .pickedUpAt(order.getPickedUpAt())
                .completedAt(order.getCompletedAt())
                .rating(order.getRating())
                .build();
    }
}
