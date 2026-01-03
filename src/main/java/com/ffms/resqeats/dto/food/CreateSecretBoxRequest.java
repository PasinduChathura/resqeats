package com.ffms.resqeats.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateSecretBoxRequest {

    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Original value is required")
    @Positive(message = "Original value must be positive")
    @JsonProperty("original_value")
    private BigDecimal originalValue;

    @NotNull(message = "Discounted price is required")
    @Positive(message = "Discounted price must be positive")
    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @JsonProperty("image_url")
    private String imageUrl;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Total quantity must be positive")
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

    @JsonProperty("may_contain")
    private String mayContain;

    @NotNull(message = "Shop ID is required")
    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("food_item_ids")
    private List<Long> foodItemIds;
}
