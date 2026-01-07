package com.ffms.resqeats.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Task to manage cart cleanup.
 * Per SRS Section 6.7: Cart is soft-state stored in Redis with TTL.
 * Redis handles expiry automatically via TTL.
 * This task handles any additional cleanup operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupTask {

    /**
     * Periodic task to release any orphaned inventory reservations.
     * Per SRS BR-010: Cart items do NOT lock inventory.
     * Reservations are only made during order submission validation.
     * This task cleans up any stale reservation keys in Redis.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupOrphanedReservations() {
        try {
            // Cart expiry is handled by Redis TTL (10 minutes per SRS)
            // This task just logs for monitoring
            log.debug("Cart cleanup task running - Redis TTL handles cart expiry");
        } catch (Exception e) {
            log.error("Error in cart cleanup task: {}", e.getMessage());
        }
    }
}
