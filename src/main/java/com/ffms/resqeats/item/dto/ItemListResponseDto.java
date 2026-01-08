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
import java.util.UUID;

/**
 * Item list response DTO with essential fields for table display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemListResponseDto {

    private UUID id;

    @JsonProperty("merchant_id")
    private UUID merchantId;

    private String name;

    private ItemCategory category;

    @JsonProperty("item_type")
    private ItemType itemType;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("sale_price")
    private BigDecimal salePrice;

    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @JsonProperty("image_url")
    private String imageUrl;

    private ItemStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Merchant association data
    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("merchant_logo_url")
    private String merchantLogoUrl;

    // Outlet availability count
    @JsonProperty("available_outlets_count")
    private Integer availableOutletsCount;
}
