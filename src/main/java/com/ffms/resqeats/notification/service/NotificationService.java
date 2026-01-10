package com.ffms.resqeats.notification.service;

import com.ffms.resqeats.notification.entity.Notification;
import com.ffms.resqeats.notification.enums.NotificationChannel;
import com.ffms.resqeats.notification.enums.NotificationStatus;
import com.ffms.resqeats.notification.enums.NotificationType;
import com.ffms.resqeats.notification.repository.NotificationRepository;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing notifications across multiple channels.
 *
 * <p>This service handles notification delivery per SRS Section 6.12, supporting
 * the following channels:</p>
 * <ul>
 *   <li>PUSH: Firebase Cloud Messaging (FCM) for mobile notifications</li>
 *   <li>EMAIL: Transactional emails (order confirmations, etc.)</li>
 *   <li>SMS: OTP verification only</li>
 *   <li>IN_APP: Stored notifications visible within the application</li>
 * </ul>
 *
 * <p>Supported notification types include:</p>
 * <ul>
 *   <li>ORDER_CREATED: Customer notification when order is created</li>
 *   <li>ORDER_ACCEPTED: Customer notification when order is accepted</li>
 *   <li>ORDER_DECLINED: Customer notification when order is declined</li>
 *   <li>ORDER_READY: Customer notification for pickup alert</li>
 *   <li>PICKUP_REMINDER: Customer reminder for pending pickup</li>
 *   <li>PICKUP_EXPIRED: Notification when pickup window expires</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Notifies outlet staff of a new incoming order.
     *
     * <p>Sends push notifications to all users associated with the outlet
     * informing them of the new order that requires review.</p>
     *
     * @param order the order that was created and needs outlet notification
     */
    @Async
    public void notifyNewOrder(Order order) {
        log.info("Sending new order notification for order: {} to outlet: {}", 
                order.getOrderNumber(), order.getOutletId());
        
        List<User> outletUsers = userRepository.findByOutletId(order.getOutletId());
        log.debug("Found {} outlet users to notify for order: {}", outletUsers.size(), order.getOrderNumber());

        for (User user : outletUsers) {
            createAndSendNotification(
                    user.getId(),
                    NotificationType.ORDER_CREATED,
                    "New Order #" + order.getOrderNumber(),
                    "You have a new order to review. Tap to see details.",
                    Map.of(
                            "orderId", order.getId().toString(),
                            "orderNumber", order.getOrderNumber(),
                            "total", order.getTotal().toString()
                    )
            );
        }

        log.info("New order notification successfully sent to {} users at outlet: {}", 
                outletUsers.size(), order.getOutletId());
    }

    /**
     * Notifies the customer that their order has been accepted.
     *
     * <p>Sends a push notification to the customer with order acceptance
     * confirmation and estimated pickup time.</p>
     *
     * @param order the order that was accepted by the outlet
     */
    @Async
    public void notifyOrderAccepted(Order order) {
        log.info("Sending order accepted notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.ORDER_ACCEPTED,
                "Order Accepted! 🎉",
                "Your order #" + order.getOrderNumber() + " has been accepted. " +
                        "Estimated pickup time: " + formatTime(order.getPickupBy()),
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber(),
                        "pickupBy", order.getPickupBy() != null ? order.getPickupBy().toString() : ""
                )
        );
        
        log.info("Order accepted notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order has been declined.
     *
     * <p>Sends a push notification to the customer informing them that the order
     * could not be fulfilled and their payment has been refunded.</p>
     *
     * @param order the order that was declined by the outlet
     */
    @Async
    public void notifyOrderDeclined(Order order) {
        log.info("Sending order declined notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.ORDER_DECLINED,
                "Order Declined",
                "Sorry, your order #" + order.getOrderNumber() + " couldn't be fulfilled. " +
                        "Your payment has been refunded.",
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber()
                )
        );
        
        log.info("Order declined notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order is being prepared.
     *
     * <p>Sends a push notification to the customer informing them that
     * the outlet has started preparing their order.</p>
     *
     * @param order the order that is currently being prepared
     */
    @Async
    public void notifyOrderPreparing(Order order) {
        log.info("Sending order preparing notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.ORDER_PREPARING,
                "Order Being Prepared 👨‍🍳",
                "Your order #" + order.getOrderNumber() + " is now being prepared!",
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber()
                )
        );
        
        log.info("Order preparing notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order is ready for pickup.
     *
     * <p>Sends a push notification to the customer with pickup code
     * and instructions to collect their order.</p>
     *
     * @param order the order that is ready for customer pickup
     */
    @Async
    public void notifyOrderReady(Order order) {
        log.info("Sending order ready notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.ORDER_READY,
                "Order Ready! 🛍️",
                "Your order #" + order.getOrderNumber() + " is ready for pickup. " +
                        "Show your pickup code: " + order.getPickupCode(),
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber(),
                        "pickupCode", order.getPickupCode() != null ? order.getPickupCode() : ""
                )
        );
        
        log.info("Order ready notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order has been completed.
     *
     * <p>Sends a thank you notification to the customer after pickup
     * and prompts them to leave a review.</p>
     *
     * @param order the order that was successfully completed
     */
    @Async
    public void notifyOrderCompleted(Order order) {
        log.info("Sending order completed notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.SYSTEM_ANNOUNCEMENT,
                "Order Completed",
                "Thanks for picking up your order! Leave a review to help others.",
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber()
                )
        );
        
        log.info("Order completed notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order has been cancelled.
     *
     * <p>Sends a notification to the customer with the cancellation reason
     * informing them that the order will not be fulfilled.</p>
     *
     * @param order the order that was cancelled
     * @param reason the reason for order cancellation
     */
    @Async
    public void notifyOrderCancelled(Order order, String reason) {
        log.info("Sending order cancelled notification to user: {} for order: {} with reason: {}", 
                order.getUserId(), order.getOrderNumber(), reason);
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.SYSTEM_ANNOUNCEMENT,
                "Order Cancelled",
                "Your order #" + order.getOrderNumber() + " has been cancelled. " +
                        "Reason: " + reason,
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber(),
                        "reason", reason != null ? reason : ""
                )
        );
        
        log.info("Order cancelled notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer that their order has expired.
     *
     * <p>Sends a notification to the customer when the pickup window
     * for their order has passed.</p>
     *
     * @param order the order that has expired
     */
    @Async
    public void notifyOrderExpired(Order order) {
        log.info("Sending order expired notification to user: {} for order: {}", 
                order.getUserId(), order.getOrderNumber());
        
        createAndSendNotification(
                order.getUserId(),
                NotificationType.PICKUP_EXPIRED,
                "Order Expired",
                "Your order #" + order.getOrderNumber() + " has expired.",
                Map.of(
                        "orderId", order.getId().toString(),
                        "orderNumber", order.getOrderNumber()
                )
        );
        
        log.info("Order expired notification successfully sent for order: {}", order.getOrderNumber());
    }

    /**
     * Notifies the customer of a successful payment.
     *
     * <p>Sends a confirmation notification to the customer when their
     * payment has been successfully processed.</p>
     *
     * @param userId the unique identifier of the user to notify
     * @param orderNumber the order number associated with the payment
     */
    @Async
        public void notifyPaymentSuccess(Long userId, String orderNumber) {
        log.info("Sending payment success notification to user: {} for order: {}", userId, orderNumber);
        
        createAndSendNotification(
                userId,
                NotificationType.PAYMENT_SUCCESS,
                "Payment Successful",
                "Your payment for order #" + orderNumber + " was successful.",
                Map.of("orderNumber", orderNumber)
        );
        
        log.info("Payment success notification successfully sent for order: {}", orderNumber);
    }

    /**
     * Notifies the customer of a failed payment.
     *
     * <p>Sends a notification to the customer when their payment
     * could not be processed, including the failure reason.</p>
     *
     * @param userId the unique identifier of the user to notify
     * @param orderNumber the order number associated with the failed payment
     * @param reason the reason for payment failure
     */
    @Async
        public void notifyPaymentFailed(Long userId, String orderNumber, String reason) {
        log.warn("Sending payment failed notification to user: {} for order: {} - reason: {}", 
                userId, orderNumber, reason);
        
        createAndSendNotification(
                userId,
                NotificationType.PAYMENT_FAILED,
                "Payment Failed",
                "Payment for order #" + orderNumber + " failed. " + reason,
                Map.of("orderNumber", orderNumber, "reason", reason != null ? reason : "")
        );
        
        log.info("Payment failed notification successfully sent for order: {}", orderNumber);
    }


    /**
     * Retrieves paginated notifications for a specific user.
     *
     * <p>Returns notifications ordered by creation date in descending order,
     * showing the most recent notifications first.</p>
     *
     * @param userId the unique identifier of the user
     * @param pageable pagination parameters for the query
     * @return a page of notifications belonging to the user
     */
        public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.info("Retrieving notifications for user: {} with page: {}", userId, pageable.getPageNumber());
        
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        log.debug("Retrieved {} notifications for user: {}", notifications.getNumberOfElements(), userId);
        return notifications;
    }

    /**
     * Gets the count of unread notifications for a user.
     *
     * <p>Returns the total number of notifications that have not been
     * marked as read by the specified user.</p>
     *
     * @param userId the unique identifier of the user
     * @return the count of unread notifications
     */
        public long getUnreadCount(Long userId) {
        log.debug("Getting unread notification count for user: {}", userId);
        
        long count = notificationRepository.countUnreadByUserId(userId);
        
        log.debug("User: {} has {} unread notifications", userId, count);
        return count;
    }

    /**
     * Marks a specific notification as read.
     *
     * <p>Updates the notification status to READ and records the timestamp.
     * Only processes if the notification exists and belongs to the specified user.</p>
     *
     * @param notificationId the unique identifier of the notification
     * @param userId the unique identifier of the user (for ownership verification)
     */
    @Transactional
        public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification: {} as read for user: {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElse(null);

        if (notification == null) {
            log.warn("Notification not found: {}", notificationId);
            return;
        }
        
        if (!notification.getUserId().equals(userId)) {
            log.warn("User: {} attempted to mark notification: {} belonging to another user", 
                    userId, notificationId);
            return;
        }
        
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        log.info("Notification: {} successfully marked as read", notificationId);
    }

    /**
     * Marks all notifications as read for a user.
     *
     * <p>Bulk updates all unread notifications for the specified user,
     * setting their status to READ with the current timestamp.</p>
     *
     * @param userId the unique identifier of the user
     */
    @Transactional
        public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        
        log.info("All notifications successfully marked as read for user: {}", userId);
    }

    /**
     * Deletes a specific notification.
     *
     * <p>Removes the notification from the database. Only processes if the
     * notification exists and belongs to the specified user.</p>
     *
     * @param notificationId the unique identifier of the notification to delete
     * @param userId the unique identifier of the user (for ownership verification)
     */
    @Transactional
        public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification: {} for user: {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElse(null);

        if (notification == null) {
            log.warn("Notification not found for deletion: {}", notificationId);
            return;
        }
        
        if (!notification.getUserId().equals(userId)) {
            log.warn("User: {} attempted to delete notification: {} belonging to another user", 
                    userId, notificationId);
            return;
        }
        
        notificationRepository.delete(notification);
        log.info("Notification: {} successfully deleted", notificationId);
    }


    /**
     * Registers an FCM push notification token for a user.
     *
     * <p>Stores the Firebase Cloud Messaging token to enable push notifications
     * for the specified user's device.</p>
     *
     * @param userId the unique identifier of the user
     * @param fcmToken the Firebase Cloud Messaging token for the device
     * @param deviceType the type of device (e.g., iOS, Android)
     */
    @Transactional
        public void registerPushToken(Long userId, String fcmToken, String deviceType) {
        log.info("Registering FCM token for user: {} on device type: {}", userId, deviceType);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for FCM token registration: {}", userId);
            return;
        }
        
        user.setFcmToken(fcmToken);
        userRepository.save(user);
        
        log.info("FCM token successfully registered for user: {}", userId);
    }

    /**
     * Unregisters the FCM push notification token for a user.
     *
     * <p>Removes the Firebase Cloud Messaging token, disabling push notifications
     * for the user's device.</p>
     *
     * @param userId the unique identifier of the user
     */
    @Transactional
        public void unregisterPushToken(Long userId) {
        log.info("Unregistering FCM token for user: {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for FCM token unregistration: {}", userId);
            return;
        }
        
        user.setFcmToken(null);
        userRepository.save(user);
        
        log.info("FCM token successfully unregistered for user: {}", userId);
    }

    /**
     * Creates and sends a notification to a user.
     *
     * <p>This internal method handles the creation of an in-app notification record
     * and attempts to send a push notification. If push notification fails, the
     * notification status is updated accordingly.</p>
     *
     * @param userId the unique identifier of the user to notify
     * @param type the type of notification being sent
     * @param title the notification title
     * @param message the notification message body
     * @param data additional data to include with the notification
     */
        private void createAndSendNotification(Long userId, NotificationType type, 
                                            String title, String message, Map<String, String> data) {
        log.debug("Creating notification for user: {} with type: {} and title: {}", userId, type, title);
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .channel(NotificationChannel.PUSH)
                .title(title)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);
        log.debug("Notification created with ID: {} for user: {}", notification.getId(), userId);

        try {
            sendPushNotification(userId, title, message, data);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.debug("Push notification successfully sent for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send push notification to user: {} for notification ID: {}", 
                    userId, notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    /**
     * Sends a push notification via Firebase Cloud Messaging.
     *
     * <p>Retrieves the user's FCM token and sends the push notification.
     * If no FCM token is available, the notification is silently skipped.</p>
     *
     * @param userId the unique identifier of the user to notify
     * @param title the notification title
     * @param body the notification body text
     * @param data additional data payload for the notification
     */
        private void sendPushNotification(Long userId, String title, String body, Map<String, String> data) {
        log.debug("Attempting to send push notification to user: {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for push notification: {}", userId);
            return;
        }
        
        if (user.getFcmToken() == null) {
            log.warn("No FCM token available for user: {}, skipping push notification", userId);
            return;
        }

        log.info("Push notification prepared for user: {} - title: {}", userId, title);
    }

    /**
     * Formats a LocalDateTime to a user-friendly time string.
     *
     * <p>Returns the time portion of the datetime, or "TBD" if the
     * datetime is null.</p>
     *
     * @param dateTime the datetime to format
     * @return formatted time string or "TBD" if null
     */
    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            log.debug("DateTime is null, returning TBD");
            return "TBD";
        }
        return dateTime.toLocalTime().toString();
    }
}
