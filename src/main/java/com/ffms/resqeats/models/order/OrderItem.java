package com.ffms.resqeats.models.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.models.food.SecretBox;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secret_box_id", nullable = false)
    private SecretBox secretBox;

    @Column(name = "secret_box_name", columnDefinition = "VARCHAR(150)")
    @JsonProperty("secret_box_name")
    private String secretBoxName;

    @Column(name = "quantity", nullable = false)
    @JsonProperty("quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @Column(name = "original_value", precision = 10, scale = 2)
    @JsonProperty("original_value")
    private BigDecimal originalValue;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
