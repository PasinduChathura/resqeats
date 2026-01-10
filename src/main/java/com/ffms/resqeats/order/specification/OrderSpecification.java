package com.ffms.resqeats.order.specification;

import com.ffms.resqeats.order.dto.OrderFilterDto;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Order entity filtering.
 * Builds dynamic queries based on OrderFilterDto.
 * 
 * SCOPE FILTERING:
 * - ADMIN/SUPER_ADMIN: No automatic scope filtering (full access)
 * - MERCHANT_USER: Automatically scoped to merchant's orders
 * - OUTLET_USER: Automatically scoped to outlet's orders
 * - CUSTOMER_USER: Automatically scoped to user's own orders
 */
public class OrderSpecification {

    /**
     * Creates a specification from a filter DTO with automatic scope filtering.
     *
     * @param filter the filter criteria
     * @return a JPA specification with scope predicates applied
     */
    public static Specification<Order> filterBy(OrderFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== AUTOMATIC SCOPE FILTERING =====
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (!context.isAnonymous() && !context.hasGlobalAccess()) {
                // MERCHANT_USER - scope to merchant's orders
                if (context.getRole() == UserRole.MERCHANT_USER && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("merchantId"), context.getMerchantId()));
                }
                // OUTLET_USER - scope to outlet's orders
                else if (context.getRole() == UserRole.OUTLET_USER && context.getOutletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("outletId"), context.getOutletId()));
                }
                // CUSTOMER_USER - scope to own orders only
                else if (context.getRole() == UserRole.CUSTOMER_USER && context.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("userId"), context.getUserId()));
                }
            }

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // ===== USER-PROVIDED FILTERS =====
            
            // Filter by userId
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));
            }

            // Filter by outletId
            if (filter.getOutletId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("outletId"), filter.getOutletId()));
            }

            // Filter by merchantId
            if (filter.getMerchantId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), filter.getMerchantId()));
            }

            // Filter by single status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filter by multiple statuses
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Filter by orderNumber
            if (filter.getOrderNumber() != null && !filter.getOrderNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderNumber")), 
                        "%" + filter.getOrderNumber().toLowerCase() + "%"));
            }

            // Filter by dateFrom (createdAt >= dateFrom)
            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.getDateFrom()));
            }

            // Filter by dateTo (createdAt <= dateTo)
            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), filter.getDateTo()));
            }

            // Filter by pickupTimeFrom
            if (filter.getPickupTimeFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("pickupTime"), filter.getPickupTimeFrom()));
            }

            // Filter by pickupTimeTo
            if (filter.getPickupTimeTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("pickupTime"), filter.getPickupTimeTo()));
            }

            // Filter by minAmount (totalAmount >= minAmount)
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("totalAmount"), filter.getMinAmount()));
            }

            // Filter by maxAmount (totalAmount <= maxAmount)
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("totalAmount"), filter.getMaxAmount()));
            }

            // Filter by paymentMethodId
            if (filter.getPaymentMethodId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethodId"), filter.getPaymentMethodId()));
            }

            // Filter by expired status
            if (filter.getExpired() != null) {
                if (filter.getExpired()) {
                    predicates.add(criteriaBuilder.lessThan(
                            root.get("expiresAt"), criteriaBuilder.currentTimestamp()));
                } else {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("expiresAt"), criteriaBuilder.currentTimestamp()));
                }
            }

            // Filter by refunded status
            if (filter.getRefunded() != null) {
                if (filter.getRefunded()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("refundedAt")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("refundedAt")));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
