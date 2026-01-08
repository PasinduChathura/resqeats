package com.ffms.resqeats.order.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.order.dto.CreateOrderRequest;
import com.ffms.resqeats.order.dto.OrderDto;
import com.ffms.resqeats.order.dto.OrderFilterDto;
import com.ffms.resqeats.order.dto.OrderListResponseDto;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.entity.OrderItem;
import com.ffms.resqeats.order.repository.OrderItemRepository;
import com.ffms.resqeats.order.service.OrderService;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.user.repository.UserRepository;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OutletRepository outletRepository;
    private final MerchantRepository merchantRepository;
    private final OrderItemRepository orderItemRepository;

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
    @Operation(summary = "List orders with filters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponseDto>>> getOrders(
            OrderFilterDto filter,
            Pageable pageable) {
        log.info("List orders request - filter: {}, page: {}", filter, pageable.getPageNumber());
        try {
            Page<Order> orders = orderService.getAllOrders(filter, pageable);
            PageResponse<OrderListResponseDto> response = PageResponse.from(orders.map(this::toListDto));
            log.info("Retrieved {} orders", orders.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Failed to list orders - Error: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get order details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable UUID orderId) {
        log.info("Get order details request for orderId: {}", orderId);
        try {
            // Scope validation is handled at repository level via Hibernate filters
            Order order = orderService.getOrderById(orderId);
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
    // Order Action Endpoints
    // =====================

    @PostMapping("/orders/{orderId}/accept")
    @Operation(summary = "Accept order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> acceptOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @RequestBody(required = false) AcceptOrderRequest request) {
        log.info("Accept order request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.acceptOrder(orderId, currentUser.getId());
            log.info("Order accepted successfully: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order accepted"));
        } catch (Exception e) {
            log.error("Failed to accept order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/decline")
    @Operation(summary = "Decline order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> declineOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody DeclineOrderRequest request) {
        log.info("Decline order request for orderId: {} - Reason: {}", orderId, request.getReason());
        try {
            Order order = orderService.declineOrder(orderId, request.getReason(), currentUser.getId());
            log.info("Order declined: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order declined"));
        } catch (Exception e) {
            log.error("Failed to decline order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/preparing")
    @Operation(summary = "Start preparing order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> startPreparing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Start preparing request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.startPreparing(orderId, currentUser.getId());
            log.info("Order preparation started: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order preparation started"));
        } catch (Exception e) {
            log.error("Failed to start preparing order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/ready")
    @Operation(summary = "Mark order ready for pickup")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> markReady(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Mark ready request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.markReady(orderId, currentUser.getId());
            log.info("Order marked ready: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order marked as ready"));
        } catch (Exception e) {
            log.error("Failed to mark order ready: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/verify")
    @Operation(summary = "Verify pickup code")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPickupCode(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody VerifyPickupRequest request) {
        log.info("Verify pickup request for orderId: {}", orderId);
        try {
            Order order = orderService.verifyPickup(orderId, request.getPickupCode(), currentUser.getId());
            log.info("Pickup verified for order: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Pickup verified"));
        } catch (Exception e) {
            log.warn("Pickup verification failed for order: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/orders/{orderId}/complete")
    @Operation(summary = "Complete order")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OrderDto>> completeOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId) {
        log.info("Complete order request for orderId: {} by userId: {}", orderId, currentUser.getId());
        try {
            Order order = orderService.completeOrder(orderId);
            log.info("Order completed: {}", order.getOrderNumber());
            return ResponseEntity.ok(ApiResponse.success(toDto(order), "Order completed"));
        } catch (Exception e) {
            log.error("Failed to complete order: {} - Error: {}", orderId, e.getMessage());
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

    // List response DTO mapping method
    private OrderListResponseDto toListDto(Order order) {
        OrderListResponseDto.OrderListResponseDtoBuilder builder = OrderListResponseDto.builder()
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
                .rating(order.getRating());

        // Add user association data
        if (order.getUserId() != null) {
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                builder.userName(user.getFirstName() + " " + user.getLastName())
                       .userEmail(user.getEmail())
                       .userPhone(user.getPhone());
            });
        }

        // Add outlet and merchant association data
        if (order.getOutletId() != null) {
            outletRepository.findById(order.getOutletId()).ifPresent(outlet -> {
                builder.outletName(outlet.getName())
                       .outletAddress(outlet.getAddress());
                
                if (outlet.getMerchantId() != null) {
                    merchantRepository.findById(outlet.getMerchantId()).ifPresent(merchant -> {
                        builder.merchantId(merchant.getId())
                               .merchantName(merchant.getName());
                    });
                }
            });
        }

        // Add items count
        java.util.List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        builder.itemsCount(items.size());

        return builder.build();
    }
}
