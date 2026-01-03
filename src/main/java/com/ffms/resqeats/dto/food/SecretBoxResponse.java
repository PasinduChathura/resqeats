package com.ffms.resqeats.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretBoxResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("original_value")
    private BigDecimal originalValue;

    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

    @JsonProperty("total_quantity")
    private Integer totalQuantity;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @JsonProperty("cutoff_time")
    private LocalTime cutoffTime;

    @JsonProperty("available_date")
    private LocalDateTime availableDate;

    @JsonProperty("expiry_time")
    private LocalDateTime expiryTime;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_visible")
    private Boolean isVisible;

    @JsonProperty("may_contain")
    private String mayContain;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_address")
    private String shopAddress;

    @JsonProperty("shop_image_url")
    private String shopImageUrl;

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("items")
    private List<SecretBoxItemResponse> items;
}
