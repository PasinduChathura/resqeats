package com.ffms.resqeats.order.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.order.dto.CreateOrderRequest;
import com.ffms.resqeats.order.dto.OrderDto;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.enums.OrderStatus;
import com.ffms.resqeats.order.service.OrderService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
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

import java.util.UUID;

/**
 * Order controller per SRS Section 6.2.
 *
 * Customer Endpoints:
 * POST /orders - Create order (checkout)
 * GET /orders - List customer orders
 * GET /orders/{orderId} - Get order details
 * POST /orders/{orderId}/cancel - Cancel order
 *
 * Outlet Endpoints:
 * GET /outlet/orders - List outlet orders
 * POST /outlet/orders/{orderId}/accept - Accept order
 * POST /outlet/orders/{orderId}/decline - Decline order
 * POST /outlet/orders/{orderId}/preparing - Start preparing
 * POST /outlet/orders/{orderId}/ready - Mark ready
 * POST /outlet/orders/{orderId}/verify - Verify pickup code
 * POST /outlet/orders/{orderId}/complete - Complete order
 */
@RestController
@RequestMapping("/api/v1")
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request for userId: {} - outletId: {}", currentUser.getId(), request.getOutletId());
        try {
            Order order = orderService.createOrder(request, currentUser.getId());
            log.info("Order created successfully: {} for userId: {}", order.getOrderNumber(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order created successfully"));
        } catch (Exception e) {
            log.error("Failed to create order for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/submit")
    @Operation(summary = "Submit order with payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDto>> submitOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody SubmitOrderRequest request) {
        log.info("Submit order request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.submitOrder(orderId, request.getPaymentMethodId());
            log.info("Order submitted successfully: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order submitted"));
        } catch (Exception e) {
            log.error("Failed to submit order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/orders")
    @Operation(summary = "List customer orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getMyOrders(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        log.info("List orders request for userId: {}, page: {}", currentUser.getId(), pageable.getPageNumber());
        try {
            Page<Order> orders = orderService.getUserOrders(currentUser.getId(), pageable);
            PageResponse<OrderDto> response = PageResponse.from(orders.map(this::toDto));
            log.info("Retrieved {} orders for userId: {}", orders.getTotalElements(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Failed to list orders for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get order details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Get order details request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            // CRITICAL-002 FIX: Authorization check BEFORE data retrieval
            // Use scoped method that validates ownership first
            Order order = orderService.getOrderByIdForUser(orderId, currentUser.getId());
            log.info("Order details retrieved successfully: {}", orderId);
            return ResponseEntity.ok(ApiResponse.success(toDto(order)));
        } catch (Exception e) {
            log.error("Failed to get order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.getReason() : "Customer requested cancellation";
        log.info("Cancel order request for orderId: {} by userId: {} - Reason: {}", orderId, currentUser.getId(), reason);
        try {
            // HIGH-002 FIX: Pass userId to enforce ownership check
            Order order = orderService.cancelOrder(orderId, reason, currentUser.getId());
            log.info("Order cancelled successfully: {}", orderId);
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order cancelled"));
        } catch (Exception e) {
            log.error("Failed to cancel order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    // =====================
    // Outlet Endpoints
    // =====================

    @GetMapping("/outlet/orders")
    @Operation(summary = "List outlet orders")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getOutletOrders(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        UUID outletId = currentUser.getOutletId();
        log.info("Outlet: List orders request for outletId: {} - status: {}, page: {}", outletId, status, pageable.getPageNumber());
        try {
            Page<Order> orders;
            if (status != null) {
                orders = orderService.getOutletOrdersByStatus(outletId, status, pageable);
            } else {
                orders = orderService.getOutletOrders(outletId, pageable);
            }
            PageResponse<OrderDto> response = PageResponse.from(orders.map(this::toDto));
            log.info("Outlet: Retrieved {} orders for outletId: {}", orders.getTotalElements(), outletId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Outlet: Failed to list orders for outletId: {} - Error: {}", outletId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/outlet/orders/pending")
    @Operation(summary = "Get pending orders for outlet")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getPendingOrders(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        UUID outletId = currentUser.getOutletId();
        log.info("Outlet: Get pending orders request for outletId: {}", outletId);
        try {
            Page<Order> orders = orderService.getOutletOrdersByStatus(outletId, OrderStatus.PENDING_OUTLET_ACCEPTANCE, pageable);
            PageResponse<OrderDto> response = PageResponse.from(orders.map(this::toDto));
            log.info("Outlet: Retrieved {} pending orders for outletId: {}", orders.getTotalElements(), outletId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Outlet: Failed to get pending orders for outletId: {} - Error: {}", outletId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/accept")
    @Operation(summary = "Accept order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> acceptOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @RequestBody(required = false) AcceptOrderRequest request) {
        log.info("Outlet: Accept order request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.acceptOrder(orderId, currentUser.getId());
            log.info("Outlet: Order accepted successfully: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order accepted"));
        } catch (Exception e) {
            log.error("Outlet: Failed to accept order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/decline")
    @Operation(summary = "Decline order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> declineOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody DeclineOrderRequest request) {
        log.info("Outlet: Decline order request for orderId: {} - Reason: {}", orderId, request.getReason());
        try {
            Order order = orderService.declineOrder(orderId, request.getReason(), currentUser.getId());
            log.info("Outlet: Order declined: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order declined"));
        } catch (Exception e) {
            log.error("Outlet: Failed to decline order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/preparing")
    @Operation(summary = "Start preparing order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> startPreparing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Outlet: Start preparing request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.startPreparing(orderId, currentUser.getId());
            log.info("Outlet: Order preparation started: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order preparation started"));
        } catch (Exception e) {
            log.error("Outlet: Failed to start preparing order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/ready")
    @Operation(summary = "Mark order ready for pickup")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> markReady(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Outlet: Mark ready request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.markReady(orderId, currentUser.getId());
            log.info("Outlet: Order marked ready: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order marked as ready"));
        } catch (Exception e) {
            log.error("Outlet: Failed to mark order ready: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/verify")
    @Operation(summary = "Verify pickup code")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPickupCode(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody VerifyPickupRequest request) {
        log.info("Outlet: Verify pickup request for orderId: {}", orderId);
        try {
            Order order = orderService.verifyPickup(orderId, request.getPickupCode(), currentUser.getId());
            log.info("Outlet: Pickup verified for order: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Pickup verified"));
        } catch (Exception e) {
            log.warn("Outlet: Pickup verification failed for order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlet/orders/{orderId}/complete")
    @Operation(summary = "Complete order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> completeOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Outlet: Complete order request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.completeOrder(orderId);
            log.info("Outlet: Order completed: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order completed"));
        } catch (Exception e) {
            log.error("Outlet: Failed to complete order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    // Request/Response DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitOrderRequest {
        private UUID paymentMethodId;
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
}
