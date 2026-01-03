package com.ffms.resqeats.models.food;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.food.FoodCategory;
import com.ffms.resqeats.models.shop.Shop;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem extends AuditEntity {

    @NotBlank
    @Column(name = "name", columnDefinition = "VARCHAR(150)", nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @NotNull
    @Positive
    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @Column(name = "image_url", columnDefinition = "VARCHAR(500)")
    @JsonProperty("image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", columnDefinition = "VARCHAR(30)")
    @JsonProperty("category")
    private FoodCategory category;

    @Column(name = "allergens", columnDefinition = "VARCHAR(500)")
    @JsonProperty("allergens")
    private String allergens;

    @Column(name = "dietary_info", columnDefinition = "VARCHAR(500)")
    @JsonProperty("dietary_info")
    private String dietaryInfo;

    @Column(name = "is_vegetarian")
    @JsonProperty("is_vegetarian")
    @Builder.Default
    private Boolean isVegetarian = false;

    @Column(name = "is_vegan")
    @JsonProperty("is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @Column(name = "is_gluten_free")
    @JsonProperty("is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @Column(name = "expiry_date")
    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;
}
