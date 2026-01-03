package com.ffms.resqeats.task;

import com.ffms.resqeats.enums.order.OrderStatus;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.repository.order.OrderRepository;
import com.ffms.resqeats.service.notification.NotificationService;
import com.ffms.resqeats.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryTask {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Value("${resqeats.order.shop-response-timeout-minutes:15}")
    private int shopResponseTimeoutMinutes;

    @Value("${resqeats.order.pickup-window-minutes:60}")
    private int pickupWindowMinutes;

    /**
     * Task to expire orders that shops haven't responded to within the timeout period.
     * Per SRS: Inventory is only locked after payment capture (shop acceptance).
     * For pending orders, inventory was NOT reserved, so no release needed.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void expirePendingOrders() {
        Date expiryTime = Date.from(LocalDateTime.now().minusMinutes(shopResponseTimeoutMinutes)
                .atZone(ZoneId.systemDefault()).toInstant());
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(expiryTime);

        for (Order order : expiredOrders) {
            try {
                log.info("Expiring pending order {} - shop didn't respond in time", order.getOrderNumber());
                
                // Release the payment pre-authorization
                paymentService.releasePreAuthorization(order.getId());
                
                // Per SRS 3.8: Inventory is only locked after payment capture (shop acceptance)
                // For PENDING_SHOP_ACCEPTANCE orders, inventory was NOT reserved, so no release needed
                
                // Update order status
                order.setStatus(OrderStatus.EXPIRED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason("Shop did not respond within the acceptance window");
                orderRepository.save(order);

                // Notify user
                notificationService.notifyUserOrderExpired(order);

                log.info("Order {} expired successfully", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to expire order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }

        if (!expiredOrders.isEmpty()) {
            log.info("Expired {} pending orders", expiredOrders.size());
        }
    }

    /**
     * Task to mark orders as expired if not picked up within the pickup window.
     * Per SRS BR-4: No refunds after pickup window closes.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void expireUnpickedOrders() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(pickupWindowMinutes);
        List<Order> unpickedOrders = orderRepository.findUnpickedReadyOrders(expiryTime);

        for (Order order : unpickedOrders) {
            try {
                log.info("Expiring unpicked order {} - pickup window exceeded", order.getOrderNumber());

                // Per SRS BR-4: No refunds after pickup window closes
                // Payment was already captured, no refund is processed
                
                order.setStatus(OrderStatus.EXPIRED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason("Order was not picked up within the pickup window (BR-4: No refund)");
                orderRepository.save(order);

                notificationService.notifyUserOrderExpired(order);

                log.info("Order {} marked as not picked up (no refund per BR-4)", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to mark order {} as not picked up: {}", order.getOrderNumber(), e.getMessage());
            }
        }

        if (!unpickedOrders.isEmpty()) {
            log.info("Marked {} orders as not picked up", unpickedOrders.size());
        }
    }
}
