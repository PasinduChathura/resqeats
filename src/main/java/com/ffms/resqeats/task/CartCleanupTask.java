package com.ffms.resqeats.task;

import com.ffms.resqeats.enums.cart.CartStatus;
import com.ffms.resqeats.models.cart.Cart;
import com.ffms.resqeats.repository.cart.CartRepository;
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
public class CartCleanupTask {

    private final CartRepository cartRepository;

    @Value("${resqeats.cart.expiry-minutes:10}")
    private int cartExpiryMinutes;

    /**
     * Task to clean up expired carts.
     * Per SRS Section 3.3: Cart is soft-reserved, items in cart do NOT reduce inventory.
     * Therefore, no inventory release is needed when cart expires.
     * Runs every 2 minutes.
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    @Transactional
    public void cleanupExpiredCarts() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(cartExpiryMinutes);
        List<Cart> expiredCarts = cartRepository.findExpiredCarts(expiryTime);

        for (Cart cart : expiredCarts) {
            try {
                log.debug("Cleaning up expired cart {} for user {}", cart.getId(), cart.getUser().getId());

                // Per SRS 3.3: Cart does NOT lock inventory, so no inventory release needed
                // Simply mark cart as expired
                cart.setStatus(CartStatus.EXPIRED);
                cartRepository.save(cart);

                log.info("Cart {} marked as expired", cart.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup expired cart {}: {}", cart.getId(), e.getMessage());
            }
        }

        if (!expiredCarts.isEmpty()) {
            log.info("Cleaned up {} expired carts", expiredCarts.size());
        }
    }

    /**
     * Task to remove old abandoned carts (older than 7 days).
     * Runs once daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void removeOldCarts() {
        Date cutoffDate = Date.from(LocalDateTime.now().minusDays(7)
                .atZone(ZoneId.systemDefault()).toInstant());
        int deletedCount = cartRepository.deleteOldCarts(cutoffDate);
        
        if (deletedCount > 0) {
            log.info("Removed {} old abandoned carts", deletedCount);
        }
    }
}
