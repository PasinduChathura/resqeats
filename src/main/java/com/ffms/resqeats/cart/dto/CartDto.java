package com.ffms.resqeats.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart DTO for responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("outlet_id")
    private UUID outletId;

    private List<CartItemDto> items;

    private BigDecimal subtotal;

    @JsonProperty("total_savings")
    private BigDecimal totalSavings;

    @JsonProperty("item_count")
    private int itemCount;

    @JsonProperty("total_quantity")
    private int totalQuantity;

    // Validation results (only present after validateCart)
    @JsonProperty("removed_items")
    private List<String> removedItems;

    @JsonProperty("adjusted_items")
    private List<String> adjustedItems;

    /**
     * Create empty cart.
     */
    public static CartDto empty(UUID userId) {
        return CartDto.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .totalSavings(BigDecimal.ZERO)
                .itemCount(0)
                .totalQuantity(0)
                .build();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    @JsonIgnore
    public boolean hasValidationIssues() {
        return (removedItems != null && !removedItems.isEmpty()) || 
               (adjustedItems != null && !adjustedItems.isEmpty());
    }
}
