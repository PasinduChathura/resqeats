package com.ffms.resqeats.notification.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.notification.entity.Notification;
import com.ffms.resqeats.notification.service.NotificationService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List user notifications")
    public ResponseEntity<ApiResponse<PageResponse<Notification>>> getNotifications(
            @CurrentUser CustomUserDetails currentUser,
            Pageable pageable) {
        Page<Notification> notifications = notificationService.getUserNotifications(
                currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(notifications)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@CurrentUser CustomUserDetails currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUser CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }

    @PostMapping("/token")
    @Operation(summary = "Register FCM token for push notifications")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @CurrentUser CustomUserDetails currentUser,
            @RequestBody RegisterTokenRequest request) {
        notificationService.registerPushToken(
                currentUser.getId(), 
                request.getToken(), 
                request.getDeviceType());
        return ResponseEntity.ok(ApiResponse.success(null, "Push token registered"));
    }

    @DeleteMapping("/token")
    @Operation(summary = "Unregister FCM token")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(@CurrentUser CustomUserDetails currentUser) {
        notificationService.unregisterPushToken(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Push token unregistered"));
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
