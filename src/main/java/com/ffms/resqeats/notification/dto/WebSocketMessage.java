package com.ffms.resqeats.notification.dto;

import com.ffms.resqeats.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketMessage {
    private String type;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Object data;
    private Long referenceId;
    private String referenceType;
    private Long timestamp;
}
