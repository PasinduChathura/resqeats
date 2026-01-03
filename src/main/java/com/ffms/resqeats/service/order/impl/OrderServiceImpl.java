package com.ffms.resqeats.service.order.impl;

import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.dto.order.*;
import com.ffms.resqeats.enums.cart.CartStatus;
import com.ffms.resqeats.enums.order.OrderStatus;
import com.ffms.resqeats.exception.order.OrderException;
import com.ffms.resqeats.models.cart.Cart;
import com.ffms.resqeats.models.cart.CartItem;
import com.ffms.resqeats.models.food.SecretBox;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.models.order.OrderItem;
import com.ffms.resqeats.models.payment.UserPaymentMethod;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.repository.cart.CartRepository;
import com.ffms.resqeats.repository.food.SecretBoxRepository;
import com.ffms.resqeats.repository.order.OrderRepository;
import com.ffms.resqeats.repository.payment.UserPaymentMethodRepository;
import com.ffms.resqeats.repository.shop.ShopRepository;
import com.ffms.resqeats.service.notification.NotificationService;
import com.ffms.resqeats.service.order.OrderService;
import com.ffms.resqeats.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final AppLogger appLogger = AppLogger.of(log);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final SecretBoxRepository secretBoxRepository;
    private final ShopRepository shopRepository;
    private final UserPaymentMethodRepository paymentMethodRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Value("${resqeats.order.shop-acceptance-minutes:5}")
    private int shopAcceptanceMinutes;

    @Value("${resqeats.order.service-fee:0.99}")
    private BigDecimal serviceFee;

    private static final List<OrderStatus> ACTIVE_USER_STATUSES = Arrays.asList(
            OrderStatus.PENDING_SHOP_ACCEPTANCE,
            OrderStatus.PAID,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP
    );

    private static final List<OrderStatus> ACTIVE_SHOP_STATUSES = Arrays.asList(
            OrderStatus.PENDING_SHOP_ACCEPTANCE,
            OrderStatus.PAID,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP
    );

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        appLogger.logStart("CREATE", "Order", userId);
        
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("CREATE", "Order", userId, "No active cart found");
                    return OrderException.cartNotFound();
                });

        if (cart.getItems().isEmpty()) {
            appLogger.logWarning("CREATE", "Order", userId, "Cart is empty");
            throw OrderException.emptyCart();
        }

        if (cart.isExpired()) {
            appLogger.logWarning("CREATE", "Order", userId, "Cart has expired");
            throw OrderException.cartExpired();
        }

        // Group cart items by shop (for now, we assume single shop per order)
        Map<Long, List<CartItem>> itemsByShop = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getShop().getId()));

        if (itemsByShop.size() > 1) {
            appLogger.logWarning("CREATE", "Order", userId, "Multiple shops in cart");
            throw OrderException.multipleShops();
        }

        // Validate payment method belongs to this user
        UserPaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(request.getPaymentMethodId(), userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("CREATE", "Order", userId, 
                            "Payment method not found: " + request.getPaymentMethodId());
                    return new OrderException("Payment method not found or doesn't belong to you", 
                            "PAY_002");
                });
        
        if (!paymentMethod.getIsActive()) {
            appLogger.logWarning("CREATE", "Order", userId, "Inactive payment method used");
            throw new OrderException("Payment method is not active. Please select a different payment method.", 
                    "PAY_010");
        }

        // Validate availability (read-only check per SRS - cart doesn't lock inventory)
        for (CartItem cartItem : cart.getItems()) {
            SecretBox secretBox = secretBoxRepository.findByIdWithLock(cartItem.getSecretBox().getId())
                    .orElseThrow(() -> {
                        appLogger.logError("CREATE", "Order", userId, 
                                "Secret box not found: " + cartItem.getSecretBox().getId());
                        return new OrderException("Secret box not found", "SBOX_001");
                    });

            if (!secretBox.isAvailable()) {
                appLogger.logWarning("CREATE", "Order", userId, 
                        "Secret box unavailable: " + secretBox.getName());
                throw OrderException.itemUnavailable(secretBox.getName());
            }

            if (secretBox.getQuantityAvailable() < cartItem.getQuantity()) {
                appLogger.logWarning("CREATE", "Order", userId, 
                        String.format("Insufficient quantity for %s: needed %d, available %d", 
                                secretBox.getName(), cartItem.getQuantity(), secretBox.getQuantityAvailable()));
                throw OrderException.insufficientQuantity(secretBox.getName(), secretBox.getQuantityAvailable());
            }
        }

        // Get the shop from first cart item
        CartItem firstItem = cart.getItems().iterator().next();
        Shop shop = firstItem.getShop();

        // Create order
        Order order = Order.builder()
                .user(cart.getUser())
                .shop(shop)
                .status(OrderStatus.CREATED)
                .serviceFee(serviceFee)
                .pickupStartTime(shop.getPickupStartTime())
                .pickupEndTime(shop.getPickupEndTime())
                .shopAcceptanceDeadline(LocalDateTime.now().plusMinutes(shopAcceptanceMinutes))
                .notes(request.getNotes())
                .build();

        // Add order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .secretBox(cartItem.getSecretBox())
                    .secretBoxName(cartItem.getSecretBox().getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .originalValue(cartItem.getSecretBox().getOriginalValue())
                    .build();
            orderItem.calculateTotalPrice();
            order.getItems().add(orderItem);
        }

        order.recalculateTotals();
        order = orderRepository.save(order);

        // Initiate payment pre-authorization
        try {
            paymentService.preAuthorizePayment(order, request.getPaymentMethodId());
            order.setStatus(OrderStatus.PENDING_SHOP_ACCEPTANCE);
            order = orderRepository.save(order);
            appLogger.info("Payment pre-authorized for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            appLogger.logError("CREATE", "Order", order.getOrderNumber(), e);
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason("Payment pre-authorization failed: " + e.getMessage());
            orderRepository.save(order);
            throw new OrderException("Payment pre-authorization failed. Please check your payment method and try again.", 
                    "PAY_006");
        }

        // Mark cart as converted
        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        // Notify shop owner
        notificationService.notifyShopNewOrder(order);

        appLogger.logSuccess("CREATE", "Order", order.getId(), 
                String.format("Order %s created for user %d, total: %s", 
                        order.getOrderNumber(), userId, order.getTotalAmount()));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        appLogger.debug("Fetching order by id: {} for user: {}", orderId, userId);
        
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> {
                    appLogger.logWarning("READ", "Order", orderId, "Order not found");
                    return OrderException.notFound(orderId);
                });

        if (!order.getUser().getId().equals(userId)) {
            appLogger.logSecurityEvent("ACCESS_DENIED", 
                    String.format("User %d attempted to access order %d", userId, orderId));
            throw OrderException.accessDenied(orderId);
        }

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber, Long userId) {
        appLogger.debug("Fetching order by number: {} for user: {}", orderNumber, userId);
        
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> {
                    appLogger.logWarning("READ", "Order", orderNumber, "Order not found");
                    return OrderException.notFoundByNumber(orderNumber);
                });

        if (!order.getUser().getId().equals(userId)) {
            appLogger.logSecurityEvent("ACCESS_DENIED", 
                    String.format("User %d attempted to access order %s", userId, orderNumber));
            throw OrderException.accessDenied(order.getId());
        }

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            orders = orderRepository.findByUserId(userId, pageable);
        }
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveUserOrders(Long userId) {
        return orderRepository.findActiveOrdersByUserId(userId, ACTIVE_USER_STATUSES).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
        appLogger.logStart("CANCEL", "Order", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    appLogger.logWarning("CANCEL", "Order", orderId, "Order not found");
                    return OrderException.notFound(orderId);
                });

        if (!order.getUser().getId().equals(userId)) {
            appLogger.logSecurityEvent("ACCESS_DENIED", 
                    String.format("User %d attempted to cancel order %d", userId, orderId));
            throw OrderException.accessDenied(orderId);
        }

        if (!order.canTransitionTo(OrderStatus.CANCELLED)) {
            appLogger.logWarning("CANCEL", "Order", orderId, 
                    "Invalid status transition from " + order.getStatus());
            throw OrderException.cannotCancel(orderId, "Order is " + order.getStatus().name().toLowerCase().replace("_", " "));
        }

        // Release payment pre-authorization
        paymentService.releasePreAuthorization(orderId);

        String previousStatus = order.getStatus().name();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        order = orderRepository.save(order);

        // Notify shop
        notificationService.notifyShopOrderCancelled(order);

        appLogger.logStateTransition("Order", orderId, previousStatus, "CANCELLED");
        appLogger.logSuccess("CANCEL", "Order", orderId, "Reason: " + reason);
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse rateOrder(Long orderId, Long userId, OrderRatingRequest request) {
        appLogger.logStart("RATE", "Order", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    appLogger.logWarning("RATE", "Order", orderId, "Order not found");
                    return OrderException.notFound(orderId);
                });

        if (!order.getUser().getId().equals(userId)) {
            appLogger.logSecurityEvent("ACCESS_DENIED", 
                    String.format("User %d attempted to rate order %d", userId, orderId));
            throw OrderException.accessDenied(orderId);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            appLogger.logWarning("RATE", "Order", orderId, 
                    "Cannot rate order in status: " + order.getStatus());
            throw OrderException.invalidStatus(order.getStatus().name(), "COMPLETED");
        }

        order.setUserRating(request.getRating());
        order.setUserFeedback(request.getFeedback());
        order = orderRepository.save(order);

        // Update shop rating
        updateShopRating(order.getShop());

        appLogger.logSuccess("RATE", "Order", orderId, 
                String.format("Rating: %d stars", request.getRating()));
        return mapToOrderResponse(order);
    }

    /**
     * Updates the shop's average rating based on all completed order ratings
     */
    private void updateShopRating(Shop shop) {
        // Get all ratings for completed orders of this shop
        Double averageRating = orderRepository.calculateAverageRatingByShopId(shop.getId());
        Integer totalRatings = orderRepository.countRatingsByShopId(shop.getId());
        
        if (averageRating != null && totalRatings != null && totalRatings > 0) {
            shop.setAverageRating(BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP));
            shop.setTotalRatings(totalRatings);
            shopRepository.save(shop);
            log.debug("Updated shop {} rating: {} ({} ratings)", shop.getId(), shop.getAverageRating(), totalRatings);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getShopOrders(Long shopId, Long ownerId, OrderStatus status, Pageable pageable) {
        validateShopOwnership(shopId, ownerId);

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByShopIdAndStatus(shopId, status, pageable);
        } else {
            orders = orderRepository.findByShopId(shopId, pageable);
        }
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getPendingShopOrders(Long shopId, Long ownerId) {
        validateShopOwnership(shopId, ownerId);
        return orderRepository.findPendingOrdersByShopId(shopId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveShopOrders(Long shopId, Long ownerId) {
        validateShopOwnership(shopId, ownerId);
        return orderRepository.findActiveOrdersByShopId(shopId, ACTIVE_SHOP_STATUSES).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse acceptOrder(Long orderId, Long ownerId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        validateShopOwnership(order.getShop().getId(), ownerId);

        if (order.getStatus() != OrderStatus.PENDING_SHOP_ACCEPTANCE) {
            throw new OrderException("Order is not pending acceptance");
        }

        // Capture payment
        try {
            paymentService.capturePayment(orderId);
        } catch (Exception e) {
            log.error("Payment capture failed for order: {}", order.getOrderNumber(), e);
            throw new OrderException("Payment capture failed: " + e.getMessage());
        }

        // Deduct inventory
        for (OrderItem item : order.getItems()) {
            SecretBox secretBox = secretBoxRepository.findByIdWithLock(item.getSecretBox().getId())
                    .orElseThrow(() -> new OrderException("Secret box not found"));
            
            if (!secretBox.reserveQuantity(item.getQuantity())) {
                // This shouldn't happen if validation was done correctly, but handle it
                paymentService.refundPayment(orderId, "Inventory unavailable");
                throw new OrderException("Insufficient inventory for: " + secretBox.getName());
            }
            secretBoxRepository.save(secretBox);
        }

        order.setStatus(OrderStatus.PAID);
        order.setAcceptedAt(LocalDateTime.now());
        
        // Set pickup deadline based on shop closing time
        LocalTime pickupEnd = order.getPickupEndTime() != null ? 
                order.getPickupEndTime() : order.getShop().getClosingTime();
        if (pickupEnd != null) {
            order.setPickupDeadline(LocalDateTime.now().toLocalDate().atTime(pickupEnd));
        }
        
        order = orderRepository.save(order);

        // Notify user
        notificationService.notifyUserOrderAccepted(order);

        log.info("Order accepted: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse declineOrder(Long orderId, Long ownerId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        validateShopOwnership(order.getShop().getId(), ownerId);

        if (order.getStatus() != OrderStatus.PENDING_SHOP_ACCEPTANCE) {
            throw new OrderException("Order is not pending acceptance");
        }

        // Release payment pre-authorization
        paymentService.releasePreAuthorization(orderId);

        order.setStatus(OrderStatus.DECLINED);
        order.setDeclinedAt(LocalDateTime.now());
        order.setDeclineReason(reason);
        order = orderRepository.save(order);

        // Notify user
        notificationService.notifyUserOrderDeclined(order, reason);

        log.info("Order declined: {} reason: {}", order.getOrderNumber(), reason);
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse markOrderPreparing(Long orderId, Long ownerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        validateShopOwnership(order.getShop().getId(), ownerId);

        if (!order.canTransitionTo(OrderStatus.PREPARING)) {
            throw new OrderException("Order cannot be marked as preparing in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Notify user
        notificationService.notifyUserOrderPreparing(order);

        log.info("Order preparing: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse markOrderReady(Long orderId, Long ownerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        validateShopOwnership(order.getShop().getId(), ownerId);

        if (!order.canTransitionTo(OrderStatus.READY_FOR_PICKUP)) {
            throw new OrderException("Order cannot be marked as ready in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setReadyAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Notify user
        notificationService.notifyUserOrderReady(order);

        log.info("Order ready for pickup: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse verifyPickup(Long orderId, String pickupCode, Long ownerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        validateShopOwnership(order.getShop().getId(), ownerId);

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new OrderException("Order is not ready for pickup");
        }

        if (!order.getPickupCode().equalsIgnoreCase(pickupCode)) {
            throw new OrderException("Invalid pickup code");
        }

        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Auto-complete the order
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Notify user
        notificationService.notifyUserOrderCompleted(order);

        log.info("Order picked up and completed: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Override
    public void processExpiredPendingOrders() {
        List<Order> expiredOrders = orderRepository.findOrdersPendingAcceptanceExpired(
                OrderStatus.PENDING_SHOP_ACCEPTANCE, LocalDateTime.now());

        for (Order order : expiredOrders) {
            try {
                paymentService.releasePreAuthorization(order.getId());
                order.setStatus(OrderStatus.EXPIRED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason("Shop acceptance timeout");
                orderRepository.save(order);

                notificationService.notifyUserOrderExpired(order);
                log.info("Expired pending order: {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to process expired order: {}", order.getOrderNumber(), e);
            }
        }
    }

    @Override
    public void processExpiredPickupOrders() {
        List<Order> expiredOrders = orderRepository.findOrdersPickupExpired(LocalDateTime.now());

        for (Order order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.EXPIRED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason("Pickup window expired");
                orderRepository.save(order);

                notificationService.notifyUserOrderExpired(order);
                log.info("Expired pickup order: {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to process expired pickup order: {}", order.getOrderNumber(), e);
            }
        }
    }

    private void validateShopOwnership(Long shopId, Long ownerId) {
        shopRepository.findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() -> new OrderException("Shop not found or you don't have permission"));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            itemResponses = order.getItems().stream()
                    .map(item -> {
                        BigDecimal savings = BigDecimal.ZERO;
                        if (item.getOriginalValue() != null && item.getUnitPrice() != null) {
                            savings = item.getOriginalValue().subtract(item.getUnitPrice())
                                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                        }
                        return OrderItemResponse.builder()
                                .id(item.getId())
                                .secretBoxId(item.getSecretBox().getId())
                                .secretBoxName(item.getSecretBoxName())
                                .secretBoxImageUrl(item.getSecretBox().getImageUrl())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .originalValue(item.getOriginalValue())
                                .savings(savings)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userName(formatUserName(order.getUser().getFirstName(), order.getUser().getLastName()))
                .userPhone(order.getUser().getPhone())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .shopAddress(order.getShop().getAddress())
                .shopPhone(order.getShop().getPhone())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .serviceFee(order.getServiceFee())
                .totalAmount(order.getTotalAmount())
                .pickupCode(order.getPickupCode())
                .pickupStartTime(order.getPickupStartTime())
                .pickupEndTime(order.getPickupEndTime())
                .pickupDeadline(order.getPickupDeadline())
                .shopAcceptanceDeadline(order.getShopAcceptanceDeadline())
                .acceptedAt(order.getAcceptedAt())
                .declinedAt(order.getDeclinedAt())
                .declineReason(order.getDeclineReason())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .pickedUpAt(order.getPickedUpAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .notes(order.getNotes())
                .userRating(order.getUserRating())
                .userFeedback(order.getUserFeedback())
                .createdAt(order.getCreatedAt() != null ? 
                        LocalDateTime.ofInstant(order.getCreatedAt().toInstant(), 
                                java.time.ZoneId.systemDefault()) : null)
                .items(itemResponses)
                .build();
    }

    /**
     * Formats user name handling null values safely
     */
    private String formatUserName(String firstName, String lastName) {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "Unknown User" : fullName;
    }
}
