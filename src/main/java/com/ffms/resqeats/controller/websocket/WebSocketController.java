package com.ffms.resqeats.controller.websocket;

import com.ffms.resqeats.dto.notification.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public WebSocketMessage handlePing(Principal principal) {
        log.debug("Ping received from user: {}", principal.getName());
        return WebSocketMessage.builder()
                .type("PONG")
                .message("Connection alive")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @MessageMapping("/subscribe/orders")
    public void subscribeToOrderUpdates(@Payload String message, Principal principal) {
        log.debug("User {} subscribed to order updates", principal.getName());
        // Client is subscribing to order updates
        // Actual updates are sent via NotificationService
    }

    @MessageMapping("/subscribe/shop")
    public void subscribeToShopUpdates(@Payload String shopId, Principal principal) {
        log.debug("User {} subscribed to shop {} updates", principal.getName(), shopId);
        // Shop owner subscribing to their shop updates
    }
}
