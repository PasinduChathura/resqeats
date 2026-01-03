package com.ffms.resqeats.models.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.order.OrderStatus;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.models.usermgt.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AuditEntity {

    @Column(name = "order_number", unique = true, nullable = false, columnDefinition = "VARCHAR(50)")
    @JsonProperty("order_number")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(30)")
    @JsonProperty("status")
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "subtotal", precision = 10, scale = 2)
    @JsonProperty("subtotal")
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "service_fee", precision = 10, scale = 2)
    @JsonProperty("service_fee")
    @Builder.Default
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @JsonProperty("total_amount")
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "pickup_code", columnDefinition = "VARCHAR(10)")
    @JsonProperty("pickup_code")
    private String pickupCode;

    @Column(name = "pickup_start_time")
    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time")
    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @Column(name = "pickup_deadline")
    @JsonProperty("pickup_deadline")
    private LocalDateTime pickupDeadline;

    @Column(name = "accepted_at")
    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "declined_at")
    @JsonProperty("declined_at")
    private LocalDateTime declinedAt;

    @Column(name = "decline_reason", columnDefinition = "VARCHAR(500)")
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

    @Column(name = "cancellation_reason", columnDefinition = "VARCHAR(500)")
    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @Column(name = "shop_acceptance_deadline")
    @JsonProperty("shop_acceptance_deadline")
    private LocalDateTime shopAcceptanceDeadline;

    @Column(name = "notes", columnDefinition = "TEXT")
    @JsonProperty("notes")
    private String notes;

    @Column(name = "user_rating")
    @JsonProperty("user_rating")
    private Integer userRating;

    @Column(name = "user_feedback", columnDefinition = "TEXT")
    @JsonProperty("user_feedback")
    private String userFeedback;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<OrderItem> items = new HashSet<>();

    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = "RQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.pickupCode == null) {
            this.pickupCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }

    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.serviceFee != null ? this.serviceFee : BigDecimal.ZERO);
    }
}
