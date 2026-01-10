package com.ffms.resqeats.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.order.enums.OrderStatus;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order entity per SRS Section 7.2.
 * Implements strict order state machine as defined in SRS Section 4.6 (FR-M-033).
 *
 * State Machine:
 * CREATED → PENDING_OUTLET_ACCEPTANCE → PAID → PREPARING → READY_FOR_PICKUP → PICKED_UP → COMPLETED
 * Alternative terminal states: DECLINED, CANCELLED, EXPIRED, REFUNDED
 * 
 * TENANT SCOPED: Filtered by outlet_id for OUTLET_USER, and by user_id for regular USER.
 * 
 * HIGH-006 FIX: Added @Version for optimistic locking to prevent race conditions in state transitions.
 * MEDIUM-004 FIX: Added composite index for outlet_id + status.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_outlet", columnList = "outlet_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_number", columnList = "order_number"),
        // MEDIUM-004 FIX: Composite index for common outlet order queries
        @Index(name = "idx_order_outlet_status", columnList = "outlet_id, status")
})
@FilterDefs({
    @FilterDef(name = "orderOutletFilter", parameters = @ParamDef(name = "outletId", type = Long.class)),
    @FilterDef(name = "orderUserFilter", parameters = @ParamDef(name = "userId", type = Long.class))
})
@Filters({
    @Filter(name = "orderOutletFilter", condition = "outlet_id = :outletId"),
    @Filter(name = "orderUserFilter", condition = "user_id = :userId")
})
@TenantScoped(TenantScopeType.OUTLET)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    /**
     * HIGH-006 FIX: Version field for optimistic locking.
     * Prevents concurrent modifications and race conditions in state transitions.
     */
    @Version
    @Column(name = "version")
    @JsonIgnore
    private Long version;

    /**
     * Human-readable order number (e.g., RQ-A1B2C3D4).
     */
    @Column(name = "order_number", length = 20, unique = true, nullable = false)
    @JsonProperty("order_number")
    private String orderNumber;

    @NotNull
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Long userId;

    @NotNull
    @Column(name = "outlet_id", nullable = false)
    @JsonProperty("outlet_id")
    private Long outletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    @JsonProperty("subtotal")
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax", precision = 10, scale = 2)
    @JsonProperty("tax")
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    @JsonProperty("total")
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * 6-digit verification code for pickup (FR-M-038).
     */
    @Column(name = "pickup_code", length = 6)
    @JsonProperty("pickup_code")
    private String pickupCode;

    /**
     * Pickup deadline timestamp (FR-M-035).
     */
    @Column(name = "pickup_by")
    @JsonProperty("pickup_by")
    private LocalDateTime pickupBy;

    /**
     * Outlet acceptance deadline (5 minutes from order creation per BR-003).
     */
    @Column(name = "acceptance_deadline")
    @JsonProperty("acceptance_deadline")
    private LocalDateTime acceptanceDeadline;

    // Timestamp tracking for state transitions
    @Column(name = "accepted_at")
    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "declined_at")
    @JsonProperty("declined_at")
    private LocalDateTime declinedAt;

    @Column(name = "decline_reason", length = 500)
    @JsonProperty("decline_reason")
    private String declineReason;

    @Column(name = "preparing_at")
    @JsonProperty("preparing_at")
    private LocalDateTime preparingAt;

    @Column(name = "ready_at")
    @JsonProperty("ready_at")
    private LocalDateTime readyAt;

    @Column(name = "picked_up_at")
    @JsonProperty("picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "completed_at")
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @Column(name = "expired_at")
    @JsonProperty("expired_at")
    private LocalDateTime expiredAt;

    // Customer feedback
    @Column(name = "rating")
    @JsonProperty("rating")
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    @JsonProperty("review")
    private String review;

    @Column(name = "review_submitted_at")
    @JsonProperty("review_submitted_at")
    private LocalDateTime reviewSubmittedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    @JsonProperty("notes")
    private String notes;

    /**
     * Validate if transition to new status is allowed.
     * Implements strict state machine per SRS FR-M-033.
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return status.canTransitionTo(newStatus);
    }

    /**
     * Check if order is in a terminal state.
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * Check if order can be cancelled.
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.CREATED || 
               status == OrderStatus.PENDING_OUTLET_ACCEPTANCE;
    }

    /**
     * Generate order number and pickup code on creation.
     */
    @PrePersist
    protected void generateOrderDetails() {
        if (orderNumber == null) {
            orderNumber = "RQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (pickupCode == null) {
            pickupCode = String.format("%06d", (int) (Math.random() * 1000000));
        }
    }
}
