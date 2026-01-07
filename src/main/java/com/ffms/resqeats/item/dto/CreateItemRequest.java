package com.ffms.resqeats.item.dto;

import com.ffms.resqeats.item.enums.ItemCategory;
import com.ffms.resqeats.item.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create item request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    @NotNull(message = "Item type is required")
    private ItemType type;

    @NotNull(message = "Original price is required")
    @Positive(message = "Original price must be positive")
    private BigDecimal originalPrice;

    @NotNull(message = "Discounted price is required")
    @Positive(message = "Discounted price must be positive")
    private BigDecimal discountedPrice;

    private String imageUrl;

    private List<String> allergens;

    private List<String> dietaryInfo;
}
