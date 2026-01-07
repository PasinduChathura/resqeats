package com.ffms.resqeats.notification.dto;

import com.ffms.resqeats.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String data;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Long referenceId;
    private String referenceType;
    private Date createdAt;
}
