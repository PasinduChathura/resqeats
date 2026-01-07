package com.ffms.resqeats.task;

import com.ffms.resqeats.order.enums.OrderStatus;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.order.repository.OrderRepository;
import com.ffms.resqeats.notification.service.NotificationService;
import com.ffms.resqeats.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task for order expiration handling.
 * 
 * MEDIUM FIX (Issue #10): Added ShedLock annotations to prevent duplicate
 * execution in multi-instance deployments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryTask {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Value("${app.order.outlet-response-timeout-minutes:5}")
    private int outletResponseTimeoutMinutes;

    @Value("${app.order.pickup-window-minutes:60}")
    private int pickupWindowMinutes;

    /**
     * Task to expire orders that outlets haven't responded to within the timeout period.
     * Per SRS BR-003: Outlets must respond within 5 minutes or order auto-cancels.
     * Per SRS BR-005: Pre-authorization is voided if outlet times out.
     * Runs every minute.
     * 
     * MEDIUM FIX: Added ShedLock to prevent duplicate execution across instances.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    @SchedulerLock(name = "expirePendingOrders", lockAtMostFor = "PT50S", lockAtLeastFor = "PT10S")
    @Transactional
    public void expirePendingOrders() {
        LocalDateTime expiryTime = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(expiryTime);

        for (Order order : expiredOrders) {
            try {
                log.info("Expiring pending order {} - outlet didn't respond in time", order.getOrderNumber());
                
                // Void the payment pre-authorization (BR-005)
                paymentService.voidPreAuthorization(order.getId());
                
                // Per SRS BR-002: Inventory is reserved only after payment capture
                // For PENDING_OUTLET_ACCEPTANCE orders, inventory was NOT reserved
                
                // Update order status
                order.setStatus(OrderStatus.EXPIRED);
                order.setExpiredAt(LocalDateTime.now());
                orderRepository.save(order);

                // Notify user
                notificationService.notifyOrderExpired(order);

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
     * Per SRS: No refunds after pickup window closes.
     * Runs every 5 minutes.
     * 
     * MEDIUM FIX: Added ShedLock to prevent duplicate execution across instances.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @SchedulerLock(name = "expireUnpickedOrders", lockAtMostFor = "PT4M", lockAtLeastFor = "PT30S")
    @Transactional
    public void expireUnpickedOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> unpickedOrders = orderRepository.findExpiredPickupOrders(now);

        for (Order order : unpickedOrders) {
            try {
                log.info("Expiring unpicked order {} - pickup window exceeded", order.getOrderNumber());

                // Payment was already captured, no refund is processed
                order.setStatus(OrderStatus.EXPIRED);
                order.setExpiredAt(LocalDateTime.now());
                orderRepository.save(order);

                notificationService.notifyOrderExpired(order);

                log.info("Order {} marked as not picked up", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to mark order {} as not picked up: {}", order.getOrderNumber(), e.getMessage());
            }
        }

        if (!unpickedOrders.isEmpty()) {
            log.info("Marked {} orders as not picked up", unpickedOrders.size());
        }
    }
}
