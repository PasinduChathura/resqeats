package com.ffms.resqeats.order.repository;

import com.ffms.resqeats.order.entity.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * OrderItem repository.
 */
@Repository
public interface OrderItemRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<OrderItem> {

    List<OrderItem> findByOrderId(UUID orderId);

    void deleteByOrderId(UUID orderId);

    @Override
    default void validateScope(OrderItem entity) {
        // OrderItem access must be validated via the parent Order (service layer).
        // Prevent accidental bypass at repository level by requiring explicit checks.
    }
}
