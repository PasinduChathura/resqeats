package com.ffms.resqeats.service.notification;

import com.ffms.resqeats.dto.notification.NotificationResponse;
import com.ffms.resqeats.enums.notification.NotificationType;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.models.shop.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    // User Notifications
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
    
    List<NotificationResponse> getUnreadNotifications(Long userId);
    
    long getUnreadCount(Long userId);
    
    void markAsRead(Long userId, Long notificationId);
    
    void markAllAsRead(Long userId);

    // Order Notifications
    void notifyShopNewOrder(Order order);
    
    void notifyUserOrderAccepted(Order order);
    
    void notifyUserOrderDeclined(Order order, String reason);
    
    void notifyUserOrderReady(Order order);
    
    void notifyUserOrderExpired(Order order);
    
    void notifyUserOrderCancelled(Order order);

    void notifyUserOrderPreparing(Order order);

    void notifyUserOrderCompleted(Order order);

    void notifyShopOrderCancelled(Order order);

    // Shop Notifications
    void notifyShopApproved(Shop shop);
    
    void notifyShopRejected(Shop shop, String reason);
    
    void notifyShopSuspended(Shop shop, String reason);

    // Generic notification
    void sendNotification(Long userId, NotificationType type, String title, String message, 
                          Long referenceId, String referenceType, Object data);
}
