package com.ffms.resqeats.item.dto;

import com.ffms.resqeats.item.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Update item request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    private String name;

    private String description;

    private ItemCategory category;

    private BigDecimal originalPrice;

    private BigDecimal discountedPrice;

    private String imageUrl;

    private List<String> allergens;

    private List<String> dietaryInfo;
}
