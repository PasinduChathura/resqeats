package com.ffms.resqeats.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.food.FoodCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItemResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

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
    private Boolean isVegetarian;

    @JsonProperty("is_vegan")
    private Boolean isVegan;

    @JsonProperty("is_gluten_free")
    private Boolean isGlutenFree;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;
}
