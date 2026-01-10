package com.ffms.resqeats.notification.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.notification.entity.Notification;
import com.ffms.resqeats.notification.enums.NotificationStatus;
import com.ffms.resqeats.notification.enums.NotificationType;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification repository per SRS Section 6.12.
 * 
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to all notifications
 * - All other roles: Access only to their own notifications (userFilter)
 */
@Repository
public interface NotificationRepository extends BaseScopedRepository<Notification> {

    // ============== USER-SCOPED METHODS ==============
    // These are automatically filtered by userFilter for all non-admin roles
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.id = :id")
    void markAsRead(@Param("id") Long id, @Param("now") LocalDateTime now);

    // ============== SYSTEM/ADMIN METHODS ==============
    // These are for system operations (notification delivery)
    
    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime before);

    List<Notification> findByOrderId(Long orderId);

    List<Notification> findByTypeAndStatusOrderByCreatedAtAsc(NotificationType type, NotificationStatus status);

    // ============== SCOPED ACCESS METHODS ==============

    /**
     * Get current user's notifications.
     */
    default Page<Notification> findMyNotifications(Pageable pageable) {
        var context = SecurityContextHolder.getContext();
        
        if (context.getUserId() != null) {
            return findByUserIdOrderByCreatedAtDesc(context.getUserId(), pageable);
        }
        
        return Page.empty(pageable);
    }

    /**
     * Get current user's unread notification count.
     */
    default long countMyUnread() {
        var context = SecurityContextHolder.getContext();
        
        if (context.getUserId() != null) {
            return countUnreadByUserId(context.getUserId());
        }
        
        return 0L;
    }

    /**
     * Mark all current user's notifications as read.
     */
    default void markAllMyNotificationsAsRead() {
        var context = SecurityContextHolder.getContext();
        
        if (context.getUserId() != null) {
            markAllAsReadByUserId(context.getUserId(), LocalDateTime.now());
        }
    }

    /**
     * Validate scope for Notification entity.
     */
    @Override
    default void validateScope(Notification entity) {
        if (entity == null) return;
        requireUserScope(entity.getUserId());
    }
}
