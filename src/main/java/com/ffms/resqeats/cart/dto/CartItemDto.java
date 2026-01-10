package com.ffms.resqeats.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cart item DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @JsonProperty("outlet_item_id")
    private Long outletItemId;

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    private int quantity;

    @JsonProperty("line_total")
    private BigDecimal lineTotal;

    @JsonProperty("line_savings")
    private BigDecimal lineSavings;
}
