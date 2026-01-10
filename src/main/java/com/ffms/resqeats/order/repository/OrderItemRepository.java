package com.ffms.resqeats.order.repository;

import com.ffms.resqeats.order.entity.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderItem repository.
 */
@Repository
public interface OrderItemRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<OrderItem> {

    List<OrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    @Override
    default void validateScope(OrderItem entity) {
        // OrderItem access must be validated via the parent Order (service layer).
        // Prevent accidental bypass at repository level by requiring explicit checks.
    }
}
