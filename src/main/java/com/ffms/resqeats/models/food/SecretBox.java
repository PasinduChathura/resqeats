package com.ffms.resqeats.models.food;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.models.shop.Shop;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secret_boxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretBox extends AuditEntity {

    @NotBlank
    @Column(name = "name", columnDefinition = "VARCHAR(150)", nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @NotNull
    @Positive
    @Column(name = "original_value", precision = 10, scale = 2, nullable = false)
    @JsonProperty("original_value")
    private BigDecimal originalValue;

    @NotNull
    @Positive
    @Column(name = "discounted_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @Column(name = "image_url", columnDefinition = "VARCHAR(500)")
    @JsonProperty("image_url")
    private String imageUrl;

    @NotNull
    @Column(name = "quantity_available", nullable = false)
    @JsonProperty("quantity_available")
    @Builder.Default
    private Integer quantityAvailable = 0;

    @NotNull
    @Column(name = "total_quantity", nullable = false)
    @JsonProperty("total_quantity")
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(name = "pickup_start_time")
    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time")
    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @Column(name = "cutoff_time")
    @JsonProperty("cutoff_time")
    private LocalTime cutoffTime;

    @Column(name = "available_date")
    @JsonProperty("available_date")
    private LocalDateTime availableDate;

    @Column(name = "expiry_time")
    @JsonProperty("expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_visible")
    @JsonProperty("is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "may_contain", columnDefinition = "VARCHAR(500)")
    @JsonProperty("may_contain")
    private String mayContain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;

    @OneToMany(mappedBy = "secretBox", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<SecretBoxItem> items = new HashSet<>();

    public boolean isAvailable() {
        return isActive && isVisible && quantityAvailable > 0 &&
                (expiryTime == null || LocalDateTime.now().isBefore(expiryTime));
    }

    public synchronized boolean reserveQuantity(int quantity) {
        if (quantityAvailable >= quantity) {
            quantityAvailable -= quantity;
            return true;
        }
        return false;
    }

    public synchronized void releaseQuantity(int quantity) {
        quantityAvailable = Math.min(quantityAvailable + quantity, totalQuantity);
    }
}
