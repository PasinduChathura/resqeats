package com.ffms.resqeats.service.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.resqeats.dto.notification.NotificationResponse;
import com.ffms.resqeats.dto.notification.WebSocketMessage;
import com.ffms.resqeats.enums.notification.NotificationType;
import com.ffms.resqeats.models.notification.Notification;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.notification.NotificationRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // ===================== User Notifications =====================

    @Override
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // ===================== Order Notifications =====================

    @Override
    public void notifyShopNewOrder(Order order) {
        Long shopOwnerId = order.getShop().getOwner().getId();
        String title = "New Order Received!";
        String message = String.format("New order #%s for LKR %.2f", 
                order.getOrderNumber(), order.getTotalAmount());

        sendNotification(shopOwnerId, NotificationType.NEW_ORDER, title, message, 
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderAccepted(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Accepted!";
        String message = String.format("Your order #%s has been accepted by %s. Pickup code: %s",
                order.getOrderNumber(), order.getShop().getName(), order.getPickupCode());

        sendNotification(userId, NotificationType.ORDER_ACCEPTED, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderDeclined(Order order, String reason) {
        Long userId = order.getUser().getId();
        String title = "Order Declined";
        String message = String.format("Your order #%s was declined. %s",
                order.getOrderNumber(), reason != null ? "Reason: " + reason : "");

        sendNotification(userId, NotificationType.ORDER_DECLINED, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderReady(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Ready for Pickup!";
        String message = String.format("Your order #%s is ready! Pickup code: %s. Visit %s to collect.",
                order.getOrderNumber(), order.getPickupCode(), order.getShop().getName());

        sendNotification(userId, NotificationType.ORDER_READY, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderExpired(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Expired";
        String message = String.format("Your order #%s has expired as it was not picked up in time.",
                order.getOrderNumber());

        sendNotification(userId, NotificationType.ORDER_EXPIRED, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderCancelled(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Cancelled";
        String message = String.format("Your order #%s has been cancelled.",
                order.getOrderNumber());

        sendNotification(userId, NotificationType.ORDER_CANCELLED, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderPreparing(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Being Prepared";
        String message = String.format("Your order #%s is being prepared at %s.",
                order.getOrderNumber(), order.getShop().getName());

        sendNotification(userId, NotificationType.ORDER_PREPARING, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyUserOrderCompleted(Order order) {
        Long userId = order.getUser().getId();
        String title = "Order Completed";
        String message = String.format("Your order #%s has been completed! Thank you for using Resqeats.",
                order.getOrderNumber());

        sendNotification(userId, NotificationType.ORDER_PICKED_UP, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    @Override
    public void notifyShopOrderCancelled(Order order) {
        Long shopOwnerId = order.getShop().getOwner().getId();
        String title = "Order Cancelled";
        String message = String.format("Order #%s has been cancelled by the customer.",
                order.getOrderNumber());

        sendNotification(shopOwnerId, NotificationType.ORDER_CANCELLED, title, message,
                order.getId(), "ORDER", buildOrderData(order));
    }

    // ===================== Shop Notifications =====================

    @Override
    public void notifyShopApproved(Shop shop) {
        Long ownerId = shop.getOwner().getId();
        String title = "Shop Approved!";
        String message = String.format("Congratulations! Your shop '%s' has been approved. You can now start adding food items.",
                shop.getName());

        sendNotification(ownerId, NotificationType.SHOP_APPROVED, title, message,
                shop.getId(), "SHOP", null);
    }

    @Override
    public void notifyShopRejected(Shop shop, String reason) {
        Long ownerId = shop.getOwner().getId();
        String title = "Shop Registration Rejected";
        String message = String.format("Your shop '%s' registration was rejected. %s",
                shop.getName(), reason != null ? "Reason: " + reason : "");

        sendNotification(ownerId, NotificationType.SHOP_REJECTED, title, message,
                shop.getId(), "SHOP", null);
    }

    @Override
    public void notifyShopSuspended(Shop shop, String reason) {
        Long ownerId = shop.getOwner().getId();
        String title = "Shop Suspended";
        String message = String.format("Your shop '%s' has been suspended. %s",
                shop.getName(), reason != null ? "Reason: " + reason : "Please contact support.");

        sendNotification(ownerId, NotificationType.SHOP_SUSPENDED, title, message,
                shop.getId(), "SHOP", null);
    }

    // ===================== Generic Notification =====================

    @Override
    @Transactional
    public void sendNotification(Long userId, NotificationType type, String title, String message,
                                  Long referenceId, String referenceType, Object data) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot send notification - user {} not found", userId);
            return;
        }

        String dataJson = null;
        if (data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                log.error("Error serializing notification data", e);
            }
        }

        // Save to database
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .data(dataJson)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // Send via WebSocket
        sendWebSocketMessage(userId, type, title, message, data, referenceId, referenceType);

        log.info("Sent {} notification to user {}: {}", type, userId, title);
    }

    // ===================== WebSocket =====================

    private void sendWebSocketMessage(Long userId, NotificationType type, String title, String message,
                                       Object data, Long referenceId, String referenceType) {
        try {
            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type("NOTIFICATION")
                    .notificationType(type)
                    .title(title)
                    .message(message)
                    .data(data)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    wsMessage
            );

            log.debug("WebSocket message sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to user {}", userId, e);
        }
    }

    // ===================== Helpers =====================

    private Object buildOrderData(Order order) {
        return java.util.Map.of(
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus().name(),
                "shopName", order.getShop().getName(),
                "totalAmount", order.getTotalAmount(),
                "pickupCode", order.getPickupCode() != null ? order.getPickupCode() : ""
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
