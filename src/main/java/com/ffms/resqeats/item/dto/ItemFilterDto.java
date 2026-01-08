package com.ffms.resqeats.item.dto;

import com.ffms.resqeats.item.enums.ItemCategory;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.item.enums.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Filter DTO for item list queries.
 * Supports comprehensive filtering for item management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item filter criteria")
public class ItemFilterDto {

    @Schema(description = "Filter by merchant ID")
    private UUID merchantId;

    @Schema(description = "Filter by outlet ID (items available at specific outlet)")
    private UUID outletId;

    @Schema(description = "Filter by item category (MEAL, BAKERY, GROCERY, BEVERAGE, OTHER)")
    private ItemCategory category;

    @Schema(description = "Filter by multiple categories")
    private List<ItemCategory> categories;

    @Schema(description = "Filter by item type (SECRET_BOX, REGULAR_ITEM)")
    private ItemType itemType;

    @Schema(description = "Filter by item status (ACTIVE, INACTIVE, OUT_OF_STOCK)")
    private ItemStatus status;

    @Schema(description = "Search in item name or description")
    private String search;

    @Schema(description = "Filter by minimum base price")
    private BigDecimal minBasePrice;

    @Schema(description = "Filter by maximum base price")
    private BigDecimal maxBasePrice;

    @Schema(description = "Filter by minimum discounted price")
    private BigDecimal minDiscountedPrice;

    @Schema(description = "Filter by maximum discounted price")
    private BigDecimal maxDiscountedPrice;

    @Schema(description = "Filter items created after this date")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter items created before this date")
    private LocalDateTime dateTo;

    @Schema(description = "Filter by minimum discount percentage")
    private BigDecimal minDiscountPercent;

    @Schema(description = "Filter items with images only")
    private Boolean hasImages;

    @Schema(description = "Filter items that are available (not out of stock)")
    private Boolean available;

    @Schema(description = "Filter by allergen-free items")
    private Boolean allergenFree;

    @Schema(description = "Filter by dietary preferences (e.g., vegan, vegetarian)")
    private String dietaryPreference;
}
