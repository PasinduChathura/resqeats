package com.ffms.resqeats.notification.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.notification.entity.Notification;
import com.ffms.resqeats.notification.service.NotificationService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Notification controller per SRS Section 6.2.
 *
 * Endpoints:
 * GET /notifications - List user notifications
 * GET /notifications/unread/count - Get unread count
 * PUT /notifications/{id}/read - Mark as read
 * PUT /notifications/read-all - Mark all as read
 * DELETE /notifications/{id} - Delete notification
 * POST /notifications/token - Register FCM token
 * DELETE /notifications/token - Unregister FCM token
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management APIs")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List user notifications")
    public ResponseEntity<ApiResponse<PageResponse<Notification>>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        log.info("Get notifications request for userId: {}, page: {}", currentUser.getId(), pageable.getPageNumber());
        try {
            Page<Notification> notifications = notificationService.getUserNotifications(
                    currentUser.getId(), pageable);
            log.info("Retrieved {} notifications for userId: {}", notifications.getTotalElements(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(notifications)));
        } catch (Exception e) {
            log.error("Failed to get notifications for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        log.info("Get unread count request for userId: {}", currentUser.getId());
        try {
            long count = notificationService.getUnreadCount(currentUser.getId());
            log.info("Unread count for userId: {} is {}", currentUser.getId(), count);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Failed to get unread count for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Mark as read request for notificationId: {} by userId: {}", id, currentUser.getId());
        try {
            notificationService.markAsRead(id, currentUser.getId());
            log.info("Notification marked as read: {} for userId: {}", id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
        } catch (Exception e) {
            log.error("Failed to mark notification as read: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        log.info("Mark all as read request for userId: {}", currentUser.getId());
        try {
            notificationService.markAllAsRead(currentUser.getId());
            log.info("All notifications marked as read for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Delete notification request for notificationId: {} by userId: {}", id, currentUser.getId());
        try {
            notificationService.deleteNotification(id, currentUser.getId());
            log.info("Notification deleted: {} for userId: {}", id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
        } catch (Exception e) {
            log.error("Failed to delete notification: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/token")
    @Operation(summary = "Register FCM token for push notifications")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody RegisterTokenRequest request) {
        log.info("Register FCM token request for userId: {} - deviceType: {}", currentUser.getId(), request.getDeviceType());
        try {
            notificationService.registerPushToken(
                    currentUser.getId(), 
                    request.getToken(), 
                    request.getDeviceType());
            log.info("FCM token registered successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Push token registered"));
        } catch (Exception e) {
            log.error("Failed to register FCM token for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/token")
    @Operation(summary = "Unregister FCM token")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(@CurrentUser UserPrincipal currentUser) {
        log.info("Unregister FCM token request for userId: {}", currentUser.getId());
        try {
            notificationService.unregisterPushToken(currentUser.getId());
            log.info("FCM token unregistered successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Push token unregistered"));
        } catch (Exception e) {
            log.error("Failed to unregister FCM token for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterTokenRequest {
        private String token;
        private String deviceType; // "ios" or "android"
    }
}
