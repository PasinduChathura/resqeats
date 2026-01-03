package com.ffms.resqeats.dto.notification;

import com.ffms.resqeats.enums.notification.NotificationType;
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
