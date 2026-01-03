package com.ffms.resqeats.models.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.notification.NotificationType;
import com.ffms.resqeats.models.usermgt.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "VARCHAR(50)", nullable = false)
    @JsonProperty("type")
    private NotificationType type;

    @Column(name = "title", columnDefinition = "VARCHAR(200)", nullable = false)
    @JsonProperty("title")
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    @JsonProperty("message")
    private String message;

    @Column(name = "data", columnDefinition = "TEXT")
    @JsonProperty("data")
    private String data; // JSON payload for additional data

    @Column(name = "is_read")
    @JsonProperty("is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    @JsonProperty("read_at")
    private LocalDateTime readAt;

    @Column(name = "reference_id")
    @JsonProperty("reference_id")
    private Long referenceId; // Order ID, Shop ID, etc.

    @Column(name = "reference_type", columnDefinition = "VARCHAR(50)")
    @JsonProperty("reference_type")
    private String referenceType; // ORDER, SHOP, etc.
}
