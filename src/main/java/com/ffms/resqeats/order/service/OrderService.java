package com.ffms.resqeats.order.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.inventory.service.InventoryService;
import com.ffms.resqeats.notification.service.NotificationService;
import com.ffms.resqeats.order.dto.*;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.entity.OrderItem;
import com.ffms.resqeats.order.enums.OrderStatus;
import com.ffms.resqeats.order.repository.OrderItemRepository;
import com.ffms.resqeats.order.repository.OrderRepository;
import com.ffms.resqeats.order.specification.OrderSpecification;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.payment.service.PaymentService;
import com.ffms.resqeats.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing order lifecycle with strict state machine transitions.
 *
 * <p>Order State Machine:</p>
 * <ul>
 *   <li>CREATED → PENDING_OUTLET_ACCEPTANCE (on order submission)</li>
 *   <li>PENDING_OUTLET_ACCEPTANCE → PAID (on outlet accept + payment capture)</li>
 *   <li>PENDING_OUTLET_ACCEPTANCE → DECLINED (on outlet decline)</li>
 *   <li>PENDING_OUTLET_ACCEPTANCE → CANCELLED (on timeout)</li>
 *   <li>PAID → PREPARING (on outlet action)</li>
 *   <li>PREPARING → READY_FOR_PICKUP (on outlet action)</li>
 *   <li>READY_FOR_PICKUP → PICKED_UP (on verification)</li>
 *   <li>PICKED_UP → COMPLETED (automatic after delay)</li>
 *   <li>READY_FOR_PICKUP → EXPIRED (on pickup window expiry)</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutletRepository outletRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;

    @Value("${app.order.acceptance-timeout-seconds:300}")
    private int acceptanceTimeoutSeconds;

    @Value("${app.order.completion-delay-minutes:5}")
    private int completionDelayMinutes;

    @Value("${resqeats.order.tax-rate:0.10}")
    private double taxRate;

    /**
     * Creates a new order from cart items with calculated totals.
     *
     * @param request the order creation request containing items and details
     * @param userId the ID of the user placing the order
     * @return the created order entity
     * @throws BusinessException if outlet not found or not accepting orders
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request, UUID userId) {
        log.info("Creating order for userId: {}, outletId: {}", userId, request.getOutletId());
        
        Outlet outlet = outletRepository.findById(request.getOutletId())
                .orElseThrow(() -> {
                    log.warn("Order creation failed - outlet not found: {}", request.getOutletId());
                    return new BusinessException("OUTLET_001", "Outlet not found");
                });

        if (!outlet.canAcceptOrders()) {
            log.warn("Order creation failed - outlet not accepting orders: {}", request.getOutletId());
            throw new BusinessException("OUTLET_002", "Outlet is not accepting orders");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            subtotal = subtotal.add(itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate));
        BigDecimal total = subtotal.add(tax);
        log.debug("Order totals calculated - subtotal: {}, tax: {}, total: {}", subtotal, tax, total);

        Order order = Order.builder()
                .userId(userId)
                .outletId(request.getOutletId())
                .status(OrderStatus.CREATED)
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .acceptanceDeadline(LocalDateTime.now().plusSeconds(acceptanceTimeoutSeconds))
                .pickupBy(request.getPickupBy())
                .notes(request.getNotes())
                .build();

        order = orderRepository.save(order);
        log.debug("Order entity saved with ID: {}", order.getId());

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .itemId(itemRequest.getItemId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .itemName(itemRequest.getItemName())
                    .build();
            orderItemRepository.save(orderItem);
        }

        log.info("Order created successfully - orderNumber: {}, userId: {}, total: {}", 
                order.getOrderNumber(), userId, total);
        return order;
    }

    /**
     * Submits an order for outlet acceptance with payment pre-authorization.
     * Transition: CREATED → PENDING_OUTLET_ACCEPTANCE
     *
     * @param orderId the order ID to submit
     * @param paymentMethodId the payment method to use for pre-authorization
     * @return the updated order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order submitOrder(UUID orderId, UUID paymentMethodId) {
        log.info("Submitting order for acceptance - orderId: {}, paymentMethodId: {}", orderId, paymentMethodId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.PENDING_OUTLET_ACCEPTANCE);

        paymentService.preAuthorize(order, paymentMethodId);
        log.debug("Payment pre-authorized for orderId: {}", orderId);

        order.setStatus(OrderStatus.PENDING_OUTLET_ACCEPTANCE);
        order.setAcceptanceDeadline(LocalDateTime.now().plusSeconds(acceptanceTimeoutSeconds));
        order = orderRepository.save(order);

        notificationService.notifyNewOrder(order);
        webSocketService.broadcastOrderUpdate(order);

        log.info("Order submitted for acceptance - orderNumber: {}, deadline: {}", 
                order.getOrderNumber(), order.getAcceptanceDeadline());
        return order;
    }

    /**
     * Outlet accepts an order - captures payment and reserves inventory.
     * Transition: PENDING_OUTLET_ACCEPTANCE → PAID
     *
     * @param orderId the order ID to accept
     * @param outletUserId the outlet user accepting the order
     * @return the updated order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order acceptOrder(UUID orderId, UUID outletUserId) {
        log.info("Accepting order - orderId: {}, outletUserId: {}", orderId, outletUserId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.PAID);

        paymentService.capturePayment(order.getId());
        log.debug("Payment captured for orderId: {}", orderId);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            inventoryService.decrementStock(item.getItemId(), item.getQuantity());
            log.debug("Inventory decremented - itemId: {}, quantity: {}", item.getItemId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        order.setAcceptedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        notificationService.notifyOrderAccepted(order);
        webSocketService.broadcastOrderUpdate(order);

        log.info("Order accepted successfully - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Outlet declines an order - releases payment pre-authorization.
     * Transition: PENDING_OUTLET_ACCEPTANCE → DECLINED
     *
     * @param orderId the order ID to decline
     * @param reason the reason for declining
     * @param outletUserId the outlet user declining the order
     * @return the updated order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order declineOrder(UUID orderId, String reason, UUID outletUserId) {
        log.info("Declining order - orderId: {}, outletUserId: {}, reason: {}", orderId, outletUserId, reason);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.DECLINED);

        paymentService.voidPreAuthorization(order.getId());
        log.debug("Payment pre-authorization voided for orderId: {}", orderId);

        order.setStatus(OrderStatus.DECLINED);
        order.setDeclinedAt(LocalDateTime.now());
        order.setDeclineReason(reason);
        order = orderRepository.save(order);

        notificationService.notifyOrderDeclined(order);
        webSocketService.broadcastOrderUpdate(order);

        log.warn("Order declined - orderNumber: {}, reason: {}", order.getOrderNumber(), reason);
        return order;
    }

    /**
     * Outlet starts preparing an order.
     * Transition: PAID → PREPARING
     *
     * @param orderId the order ID to start preparing
     * @param outletUserId the outlet user starting preparation
     * @return the updated order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order startPreparing(UUID orderId, UUID outletUserId) {
        log.info("Starting order preparation - orderId: {}, outletUserId: {}", orderId, outletUserId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.PREPARING);

        order.setStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());
        order = orderRepository.save(order);

        notificationService.notifyOrderPreparing(order);
        webSocketService.broadcastOrderUpdate(order);

        log.info("Order preparation started - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Marks an order as ready for pickup.
     * Transition: PREPARING → READY_FOR_PICKUP
     *
     * @param orderId the order ID to mark ready
     * @param outletUserId the outlet user marking the order ready
     * @return the updated order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order markReady(UUID orderId, UUID outletUserId) {
        log.info("Marking order ready for pickup - orderId: {}, outletUserId: {}", orderId, outletUserId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.READY_FOR_PICKUP);

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setReadyAt(LocalDateTime.now());
        order = orderRepository.save(order);

        notificationService.notifyOrderReady(order);
        webSocketService.broadcastOrderUpdate(order);

        log.info("Order ready for pickup - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Verifies customer pickup with code and transitions order.
     * Transition: READY_FOR_PICKUP → PICKED_UP
     *
     * @param orderId the order ID to verify pickup
     * @param pickupCode the pickup verification code
     * @param outletUserId the outlet user verifying pickup
     * @return the updated order entity
     * @throws BusinessException if invalid pickup code or invalid state transition
     */
    @Transactional
    public Order verifyPickup(UUID orderId, String pickupCode, UUID outletUserId) {
        log.info("Verifying pickup - orderId: {}, outletUserId: {}", orderId, outletUserId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.PICKED_UP);

        if (!order.getPickupCode().equals(pickupCode)) {
            log.warn("Pickup verification failed - invalid code for orderId: {}", orderId);
            throw new BusinessException("ORDER_008", "Invalid pickup code");
        }

        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());
        order = orderRepository.save(order);

        webSocketService.broadcastOrderUpdate(order);

        log.info("Order picked up successfully - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Completes an order (called automatically after pickup delay).
     * Transition: PICKED_UP → COMPLETED
     *
     * @param orderId the order ID to complete
     * @return the completed order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order completeOrder(UUID orderId) {
        log.info("Completing order - orderId: {}", orderId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.COMPLETED);

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        webSocketService.broadcastOrderUpdate(order);

        log.info("Order completed successfully - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Cancels an order (by customer).
     * Transitions: CREATED/PENDING_OUTLET_ACCEPTANCE → CANCELLED
     *
     * @param orderId the order ID to cancel
     * @param reason the cancellation reason
     * @param userId the user requesting cancellation
     * @return the cancelled order entity
     * @throws BusinessException if not authorized or order cannot be cancelled
     */
    @Transactional
    public Order cancelOrder(UUID orderId, String reason, UUID userId) {
        log.info("Processing order cancellation - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);
        Order order = getOrderById(orderId);
        
        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized cancellation attempt - orderId: {}, userId: {}", orderId, userId);
            throw new BusinessException("AUTH_003", "Not authorized to cancel this order");
        }
        
        if (!order.canBeCancelled()) {
            log.warn("Cancellation rejected - order cannot be cancelled in current state: {}", order.getStatus());
            throw new BusinessException("ORDER_005", "Order cannot be cancelled in current state");
        }

        if (order.getStatus() == OrderStatus.PENDING_OUTLET_ACCEPTANCE) {
            paymentService.voidPreAuthorization(order.getId());
            log.debug("Payment pre-authorization voided for orderId: {}", orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        order = orderRepository.save(order);

        webSocketService.broadcastOrderUpdate(order);

        log.info("Order cancelled successfully - orderNumber: {}, reason: {}", order.getOrderNumber(), reason);
        return order;
    }
    
    /**
     * Cancels an order by system (no ownership check - for scheduled tasks).
     *
     * @param orderId the order ID to cancel
     * @param reason the system cancellation reason
     * @return the cancelled order entity
     * @throws BusinessException if order cannot be cancelled
     */
    @Transactional
    public Order cancelOrderBySystem(UUID orderId, String reason) {
        log.info("Processing system order cancellation - orderId: {}, reason: {}", orderId, reason);
        Order order = getOrderById(orderId);
        
        if (!order.canBeCancelled()) {
            log.warn("System cancellation rejected - order cannot be cancelled in current state: {}", order.getStatus());
            throw new BusinessException("ORDER_005", "Order cannot be cancelled in current state");
        }

        if (order.getStatus() == OrderStatus.PENDING_OUTLET_ACCEPTANCE) {
            paymentService.voidPreAuthorization(order.getId());
            log.debug("Payment pre-authorization voided for orderId: {}", orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        order = orderRepository.save(order);

        webSocketService.broadcastOrderUpdate(order);

        log.warn("Order cancelled by system - orderNumber: {}, reason: {}", order.getOrderNumber(), reason);
        return order;
    }

    /**
     * Expires an order (pickup window exceeded).
     * Transition: READY_FOR_PICKUP → EXPIRED
     *
     * @param orderId the order ID to expire
     * @return the expired order entity
     * @throws BusinessException if order not found or invalid state transition
     */
    @Transactional
    public Order expireOrder(UUID orderId) {
        log.info("Processing order expiration - orderId: {}", orderId);
        Order order = getOrderById(orderId);
        validateTransition(order, OrderStatus.EXPIRED);

        order.setStatus(OrderStatus.EXPIRED);
        order.setExpiredAt(LocalDateTime.now());
        order = orderRepository.save(order);

        notificationService.notifyOrderExpired(order);
        webSocketService.broadcastOrderUpdate(order);

        log.warn("Order expired - orderNumber: {}", order.getOrderNumber());
        return order;
    }

    /**
     * Retrieves an order by ID.
     *
     * @param orderId the order ID to retrieve
     * @return the order entity
     * @throws BusinessException if order not found
     */
    public Order getOrderById(UUID orderId) {
        log.debug("Retrieving order - orderId: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found - orderId: {}", orderId);
                    return new BusinessException("ORDER_003", "Order not found");
                });
    }
    
    /**
     * Retrieves an order by ID for a specific user with authorization check.
     *
     * @param orderId the order ID to retrieve
     * @param userId the user ID for authorization
     * @return the order entity
     * @throws BusinessException if order not found or not authorized
     */
    public Order getOrderByIdForUser(UUID orderId, UUID userId) {
        log.debug("Retrieving order for user - orderId: {}, userId: {}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found - orderId: {}", orderId);
                    return new BusinessException("ORDER_003", "Order not found");
                });
        
        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt - orderId: {}, userId: {}", orderId, userId);
            throw new BusinessException("AUTH_003", "Not authorized to access this order");
        }
        
        return order;
    }

    /**
     * Retrieves an order by order number.
     *
     * @param orderNumber the order number to search for
     * @return the order entity
     * @throws BusinessException if order not found
     */
    public Order getOrderByNumber(String orderNumber) {
        log.debug("Retrieving order by number - orderNumber: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("Order not found - orderNumber: {}", orderNumber);
                    return new BusinessException("ORDER_003", "Order not found");
                });
    }

    /**
     * Retrieves all orders with comprehensive filtering.
     *
     * @param filter the filter criteria
     * @param pageable the pagination parameters
     * @return a page of filtered orders
     */
    public Page<Order> getAllOrders(OrderFilterDto filter, Pageable pageable) {
        log.info("Retrieving all orders with filter: {}, page: {}, size: {}", 
                filter, pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> orders = orderRepository.findAll(OrderSpecification.filterBy(filter), pageable);
        log.info("Retrieved {} orders", orders.getTotalElements());
        return orders;
    }

    /**
     * Retrieves paginated orders for a user.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of user orders
     */
    public Page<Order> getUserOrders(UUID userId, Pageable pageable) {
        log.info("Retrieving orders for userId: {}, page: {}", userId, pageable.getPageNumber());
        Page<Order> result = orderRepository.findByUserId(userId, pageable);
        log.debug("Retrieved {} orders for userId: {}", result.getTotalElements(), userId);
        return result;
    }

    /**
     * Retrieves paginated orders for an outlet.
     *
     * @param outletId the outlet ID
     * @param pageable pagination parameters
     * @return page of outlet orders
     */
    public Page<Order> getOutletOrders(UUID outletId, Pageable pageable) {
        log.info("Retrieving orders for outletId: {}, page: {}", outletId, pageable.getPageNumber());
        Page<Order> result = orderRepository.findByOutletId(outletId, pageable);
        log.debug("Retrieved {} orders for outletId: {}", result.getTotalElements(), outletId);
        return result;
    }

    /**
     * Retrieves paginated orders for an outlet filtered by status.
     *
     * @param outletId the outlet ID
     * @param status the order status filter
     * @param pageable pagination parameters
     * @return page of filtered outlet orders
     */
    public Page<Order> getOutletOrdersByStatus(UUID outletId, OrderStatus status, Pageable pageable) {
        log.info("Retrieving orders for outletId: {}, status: {}", outletId, status);
        Page<Order> result = orderRepository.findByOutletIdAndStatus(outletId, status, pageable);
        log.debug("Retrieved {} orders for outletId: {} with status: {}", result.getTotalElements(), outletId, status);
        return result;
    }

    /**
     * Retrieves active orders for a user.
     *
     * @param userId the user ID
     * @return list of active orders
     */
    public List<Order> getActiveOrdersForUser(UUID userId) {
        log.info("Retrieving active orders for userId: {}", userId);
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.CREATED,
                OrderStatus.PENDING_OUTLET_ACCEPTANCE,
                OrderStatus.PAID,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.PICKED_UP
        );
        List<Order> result = orderRepository.findActiveOrdersByUserId(userId, activeStatuses);
        log.debug("Retrieved {} active orders for userId: {}", result.size(), userId);
        return result;
    }

    /**
     * Submits a rating and review for a completed order.
     *
     * @param orderId the order ID to review
     * @param rating the rating (1-5)
     * @param review the review text
     * @param userId the user submitting the review
     * @return the updated order entity
     * @throws BusinessException if not authorized, order not completed, or review period expired
     */
    @Transactional
    public Order submitReview(UUID orderId, Integer rating, String review, UUID userId) {
        log.info("Submitting review - orderId: {}, userId: {}, rating: {}", orderId, userId, rating);
        Order order = getOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            log.warn("Unauthorized review attempt - orderId: {}, userId: {}", orderId, userId);
            throw new BusinessException("AUTH_003", "Not authorized to review this order");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("Review rejected - order not completed: {}", order.getStatus());
            throw new BusinessException("ORDER_003", "Can only review completed orders");
        }

        if (order.getPickedUpAt() != null && 
            order.getPickedUpAt().plusHours(48).isBefore(LocalDateTime.now())) {
            log.warn("Review rejected - review period expired for orderId: {}", orderId);
            throw new BusinessException("ORDER_012", "Review period has expired");
        }

        order.setRating(rating);
        order.setReview(review);
        order.setReviewSubmittedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        log.info("Review submitted successfully - orderNumber: {}, rating: {}", order.getOrderNumber(), rating);
        return order;
    }

    /**
     * Validates a state transition for an order.
     *
     * @param order the order to validate
     * @param newStatus the target status
     * @throws BusinessException if transition is not valid
     */
    private void validateTransition(Order order, OrderStatus newStatus) {
        if (!order.canTransitionTo(newStatus)) {
            log.error("Invalid state transition - from: {} to: {} for orderId: {}", 
                    order.getStatus(), newStatus, order.getId());
            throw new BusinessException("ORDER_003",
                    String.format("Invalid state transition from %s to %s", 
                            order.getStatus(), newStatus));
        }
    }
}
