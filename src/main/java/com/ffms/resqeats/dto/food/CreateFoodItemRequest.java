package com.ffms.resqeats.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.food.FoodCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFoodItemRequest {

    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Original price is required")
    @Positive(message = "Original price must be positive")
    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("category")
    private FoodCategory category;

    @JsonProperty("allergens")
    private String allergens;

    @JsonProperty("dietary_info")
    private String dietaryInfo;

    @JsonProperty("is_vegetarian")
    @Builder.Default
    private Boolean isVegetarian = false;

    @JsonProperty("is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @JsonProperty("is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;

    @NotNull(message = "Shop ID is required")
    @JsonProperty("shop_id")
    private Long shopId;
}
