package com.ffms.resqeats.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.item.enums.ItemCategory;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.item.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Item response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private UUID id;

    @JsonProperty("merchant_id")
    private UUID merchantId;

    private String name;

    private String description;

    private ItemCategory category;

    private ItemType type;

    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @JsonProperty("discount_percent")
    private int discountPercent;

    @JsonProperty("image_url")
    private String imageUrl;

    private List<String> allergens;

    @JsonProperty("dietary_info")
    private List<String> dietaryInfo;

    private ItemStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
