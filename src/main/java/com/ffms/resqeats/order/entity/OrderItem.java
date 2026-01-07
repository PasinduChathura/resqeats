package com.ffms.resqeats.order.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OrderItem entity per SRS Section 7.2.
 * Line items within an order.
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_item", columnList = "item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @NotNull
    @Column(name = "order_id", nullable = false)
    @JsonProperty("order_id")
    private UUID orderId;

    @NotNull
    @Column(name = "item_id", nullable = false)
    @JsonProperty("item_id")
    private UUID itemId;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false)
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * Price at time of order (snapshot).
     */
    @NotNull
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    /**
     * Line item total (quantity * unit_price).
     */
    @NotNull
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    /**
     * Item name snapshot (in case item is later modified).
     */
    @Column(name = "item_name", length = 255)
    @JsonProperty("item_name")
    private String itemName;

    /**
     * Calculate total price.
     */
    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
