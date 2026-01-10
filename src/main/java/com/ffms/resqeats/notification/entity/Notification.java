package com.ffms.resqeats.notification.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.notification.enums.NotificationChannel;
import com.ffms.resqeats.notification.enums.NotificationStatus;
import com.ffms.resqeats.notification.enums.NotificationType;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

/**
 * Notification entity per SRS Section 4.8 and 6.12.
 * Supports push notifications, WebSocket, and email channels.
 * 
 * TENANT SCOPED: Filtered by user_id for USER role.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_status", columnList = "status")
})
@FilterDef(name = "notificationUserFilter", parameters = @ParamDef(name = "userId", type = Long.class))
@Filter(name = "notificationUserFilter", condition = "user_id = :userId")
@TenantScoped(TenantScopeType.USER)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    @JsonProperty("type")
    private NotificationType type;

    @Column(name = "title", length = 255)
    @JsonProperty("title")
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    @JsonProperty("message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    @JsonProperty("channel")
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Related order ID (if applicable).
     */
    @Column(name = "order_id")
    @JsonProperty("order_id")
    private Long orderId;

    /**
     * Related outlet ID (if applicable).
     */
    @Column(name = "outlet_id")
    @JsonProperty("outlet_id")
    private Long outletId;

    @Column(name = "sent_at")
    @JsonProperty("sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    @JsonProperty("read_at")
    private LocalDateTime readAt;

    /**
     * External reference (e.g., FCM message ID).
     */
    @Column(name = "external_ref", length = 255)
    @JsonProperty("external_ref")
    private String externalRef;

    /**
     * Failure reason if delivery failed.
     */
    @Column(name = "failure_reason", length = 500)
    @JsonProperty("failure_reason")
    private String failureReason;

    /**
     * Additional data as JSON.
     */
    @Column(name = "data", columnDefinition = "TEXT")
    @JsonProperty("data")
    private String data;

    /**
     * Check if notification has been read.
     */
    public boolean isRead() {
        return readAt != null;
    }
}
