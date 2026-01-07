package com.ffms.resqeats.config;

import com.ffms.resqeats.jwt.JwtUtils;
import com.ffms.resqeats.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket configuration with authentication and authorization.
 * 
 * HIGH-004 FIX: Added subscription authorization to prevent users from 
 * subscribing to topics they shouldn't have access to.
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for user-specific destinations
        config.enableSimpleBroker("/queue", "/topic");
        
        // Application destination prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for clients to connect
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Native WebSocket endpoint (without SockJS fallback)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor == null) {
                    return message;
                }
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from header
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        
                        try {
                            if (jwtUtils.validateJwtToken(jwt)) {
                                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);
                                
                                log.debug("WebSocket authentication successful for user: {}", username);
                            }
                        } catch (Exception e) {
                            log.error("WebSocket authentication failed", e);
                        }
                    }
                }
                
                // HIGH-004 FIX: Authorize subscription to user-specific topics
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    Principal user = accessor.getUser();
                    
                    if (destination != null && user != null) {
                        authorizeSubscription(destination, user);
                    } else if (destination != null && !isPublicTopic(destination)) {
                        // Unauthenticated users can't subscribe to non-public topics
                        log.warn("Unauthenticated subscription attempt to: {}", destination);
                        throw new AccessDeniedException("Authentication required for this subscription");
                    }
                }
                
                return message;
            }
        });
    }
    
    /**
     * HIGH-004 FIX: Validate subscription authorization based on topic pattern.
     * - /user/{userId}/... → Only the user themselves can subscribe
     * - /topic/orders/{outletId} → Only outlet staff or admins
     * - /topic/order/{orderId} → User must own the order (handled in service layer)
     * - /topic/inventory/{outletId} → Only outlet staff or admins
     */
    private void authorizeSubscription(String destination, Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken auth)) {
            if (!isPublicTopic(destination)) {
                throw new AccessDeniedException("Authentication required");
            }
            return;
        }
        
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails userDetails)) {
            return;
        }
        
        // User-specific queues - must match user ID
        if (destination.startsWith("/user/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 3) {
                String destUserId = parts[2];
                UUID actualUserId = userDetails.getId();
                
                // Allow if user ID matches or if user is admin
                if (!destUserId.equals(actualUserId.toString()) && !hasAdminRole(userDetails)) {
                    log.warn("User {} attempted to subscribe to another user's queue: {}", 
                            actualUserId, destination);
                    throw new AccessDeniedException("Cannot subscribe to another user's queue");
                }
            }
        }
        
        // Outlet-specific topics (orders, inventory)
        if (destination.matches("/topic/(orders|inventory)/.*")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                String outletIdStr = parts[3];
                
                // Admin/SuperAdmin can subscribe to any outlet
                if (!hasAdminRole(userDetails)) {
                    // Outlet users must be assigned to this outlet
                    UUID assignedOutlet = userDetails.getOutletId();
                    if (assignedOutlet == null || !assignedOutlet.toString().equals(outletIdStr)) {
                        log.warn("User {} attempted to subscribe to outlet {} but is assigned to {}", 
                                userDetails.getId(), outletIdStr, assignedOutlet);
                        throw new AccessDeniedException("Not authorized for this outlet");
                    }
                }
            }
        }
        
        log.debug("Subscription authorized: {} for user {}", destination, userDetails.getUsername());
    }
    
    /**
     * Check if topic is public (doesn't require authorization)
     */
    private boolean isPublicTopic(String destination) {
        // No public topics for now - all require authentication
        return false;
    }
    
    /**
     * Check if user has admin role
     */
    private boolean hasAdminRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") 
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
