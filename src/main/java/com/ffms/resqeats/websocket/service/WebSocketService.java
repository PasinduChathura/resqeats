package com.ffms.resqeats.websocket.service;

import com.ffms.resqeats.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * WebSocket service for real-time updates per SRS Section 6.11.
 *
 * <p>This service provides real-time communication capabilities using WebSocket
 * protocol to push updates to connected clients. It supports broadcasting order
 * updates, inventory changes, and personal notifications.</p>
 *
 * <p>Supported Topics:</p>
 * <ul>
 *   <li>/topic/orders/{outletId} - New orders for outlet (outlet staff subscription)</li>
 *   <li>/topic/order/{orderId} - Order status updates (customer subscription)</li>
 *   <li>/topic/inventory/{outletId} - Inventory updates (real-time stock)</li>
 *   <li>/user/{userId}/queue/notifications - Personal notifications</li>
 * </ul>
 *
 * <p>All broadcast methods are marked as @Async to prevent blocking transaction completion.</p>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts order update to relevant parties including customer and outlet.
     *
     * <p>This method sends order status updates to both the customer who placed the order
     * and the outlet handling the order. The method is asynchronous to prevent blocking
     * the calling transaction.</p>
     *
     * @param order the order entity containing update information
     */
    @Async
    public void broadcastOrderUpdate(Order order) {
        log.info("Broadcasting order update for order: {}, status: {}", 
                order.getOrderNumber(), order.getStatus());
        log.debug("Order update details - orderId: {}, userId: {}, outletId: {}", 
                order.getId(), order.getUserId(), order.getOutletId());

        sendToUser(order.getUserId(), "order-update", Map.of(
                "orderId", order.getId().toString(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus().name(),
                "statusDisplay", order.getStatus().name().replace("_", " "),
                "updatedAt", order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : ""
        ));

        sendToTopic("orders/" + order.getOutletId(), Map.of(
                "orderId", order.getId().toString(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus().name(),
                "statusDisplay", order.getStatus().name().replace("_", " ")
        ));

        log.info("Order update broadcast completed for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies outlet of a new incoming order.
     *
     * <p>This method sends a notification to the outlet staff when a new order is placed.
     * The notification includes basic order information such as order number, total amount,
     * and expected pickup time. The method is asynchronous to prevent blocking the calling
     * transaction.</p>
     *
     * @param order the new order entity to notify about
     */
    @Async
    public void notifyNewOrder(Order order) {
        log.info("Notifying outlet: {} of new order: {}", order.getOutletId(), order.getOrderNumber());
        log.debug("New order details - orderId: {}, total: {}, pickupBy: {}", 
                order.getId(), order.getTotal(), order.getPickupBy());

        sendToTopic("orders/" + order.getOutletId(), Map.of(
                "type", "NEW_ORDER",
                "orderId", order.getId().toString(),
                "orderNumber", order.getOrderNumber(),
                "total", order.getTotal().toString(),
                "pickupBy", order.getPickupBy() != null ? order.getPickupBy().toString() : ""
        ));

        log.info("New order notification sent successfully to outlet: {}", order.getOutletId());
    }

    /**
     * Broadcasts inventory update for a specific item at an outlet.
     *
     * <p>This method sends real-time inventory quantity updates to all subscribers
     * monitoring the outlet's inventory. The method is asynchronous to prevent blocking
     * the calling transaction.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @param itemId the unique identifier of the item
     * @param newQuantity the updated quantity of the item
     */
    @Async
        public void broadcastInventoryUpdate(Long outletId, Long itemId, int newQuantity) {
        log.info("Broadcasting inventory update for outlet: {}, item: {}, quantity: {}", 
                outletId, itemId, newQuantity);

        if (newQuantity <= 0) {
            log.warn("Item quantity is zero or negative for outlet: {}, item: {}", outletId, itemId);
        }

        sendToTopic("inventory/" + outletId, Map.of(
                "itemId", itemId.toString(),
                "quantity", newQuantity,
                "inStock", newQuantity > 0
        ));

        log.info("Inventory update broadcast completed for outlet: {}, item: {}", outletId, itemId);
    }

    /**
     * Broadcasts a sold out notification for a specific item at an outlet.
     *
     * <p>This method notifies all subscribers that a particular item is no longer
     * available at the specified outlet. This enables real-time menu updates for
     * customers viewing the outlet's offerings.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @param itemId the unique identifier of the sold out item
     * @param itemName the display name of the sold out item
     */
        public void broadcastItemSoldOut(Long outletId, Long itemId, String itemName) {
        log.info("Broadcasting sold out notification for outlet: {}, item: {} ({})", 
                outletId, itemId, itemName);

        sendToTopic("inventory/" + outletId, Map.of(
                "type", "SOLD_OUT",
                "itemId", itemId.toString(),
                "itemName", itemName,
                "quantity", 0,
                "inStock", false
        ));

        log.info("Sold out notification broadcast completed for item: {} at outlet: {}", itemName, outletId);
    }

    /**
     * Sends a personal notification to a specific user.
     *
     * <p>This method delivers a notification directly to a user's personal queue.
     * The notification includes a title, body message, and optional additional data
     * for the client application to process.</p>
     *
     * @param userId the unique identifier of the target user
     * @param title the notification title
     * @param body the notification body message
     * @param data additional data to include with the notification
     */
        public void sendNotificationToUser(Long userId, String title, String body, Map<String, Object> data) {
        log.info("Sending notification to user: {}, title: {}", userId, title);
        log.debug("Notification details - body: {}, data keys: {}", body, data.keySet());

        sendToUser(userId, "notification", Map.of(
                "title", title,
                "body", body,
                "data", data
        ));

        log.info("Notification sent successfully to user: {}", userId);
    }

    /**
     * Broadcasts outlet status change notification.
     *
     * <p>This method notifies all subscribers when an outlet changes its operational
     * status between open and closed. This enables real-time updates for customers
     * browsing available outlets.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @param isOpen true if the outlet is now open, false if closed
     */
        public void broadcastOutletStatusChange(Long outletId, boolean isOpen) {
        log.info("Broadcasting outlet status change for outlet: {}, isOpen: {}", outletId, isOpen);

        sendToTopic("outlet/" + outletId, Map.of(
                "type", "STATUS_CHANGE",
                "outletId", outletId.toString(),
                "isOpen", isOpen
        ));

        log.info("Outlet status change broadcast completed for outlet: {}", outletId);
    }

    /**
     * Broadcasts order countdown timer update to customer.
     *
     * <p>This method sends real-time countdown updates to customers for their active
     * orders, showing the remaining time until expected pickup or completion.</p>
     *
     * @param order the order entity for which countdown is being sent
     * @param secondsRemaining the number of seconds remaining until pickup time
     */
    public void broadcastOrderCountdown(Order order, long secondsRemaining) {
        log.info("Broadcasting order countdown for order: {}, seconds remaining: {}", 
                order.getOrderNumber(), secondsRemaining);
        log.debug("Countdown details - orderId: {}, userId: {}, status: {}", 
                order.getId(), order.getUserId(), order.getStatus());

        if (secondsRemaining <= 0) {
            log.warn("Order countdown has expired for order: {}", order.getOrderNumber());
        }

        sendToUser(order.getUserId(), "order-countdown", Map.of(
                "orderId", order.getId().toString(),
                "orderNumber", order.getOrderNumber(),
                "secondsRemaining", secondsRemaining,
                "status", order.getStatus().name()
        ));

        log.debug("Order countdown broadcast completed for order: {}", order.getOrderNumber());
    }

    /**
     * Sends a message to a specific WebSocket topic.
     *
     * <p>This is a private helper method that wraps the messaging template's
     * convertAndSend functionality with error handling and logging.</p>
     *
     * @param topic the topic path (without the /topic/ prefix)
     * @param payload the message payload to send
     */
    private void sendToTopic(String topic, Map<String, Object> payload) {
        log.debug("Sending message to topic: /topic/{}", topic);

        try {
            messagingTemplate.convertAndSend("/topic/" + topic, payload);
            log.debug("Message sent successfully to topic: /topic/{}", topic);
        } catch (Exception e) {
            log.error("Failed to send message to topic: /topic/{}, error: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * Sends a message to a specific user's queue.
     *
     * <p>This is a private helper method that wraps the messaging template's
     * convertAndSendToUser functionality with error handling and logging.</p>
     *
     * @param userId the unique identifier of the target user
     * @param destination the queue destination (without the /queue/ prefix)
     * @param payload the message payload to send
     */
        private void sendToUser(Long userId, String destination, Map<String, Object> payload) {
        log.debug("Sending message to user: {}, destination: /queue/{}", userId, destination);

        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/" + destination,
                    payload
            );
            log.debug("Message sent successfully to user: {}, destination: /queue/{}", userId, destination);
        } catch (Exception e) {
            log.error("Failed to send message to user: {}, destination: /queue/{}, error: {}", 
                    userId, destination, e.getMessage(), e);
        }
    }
}
