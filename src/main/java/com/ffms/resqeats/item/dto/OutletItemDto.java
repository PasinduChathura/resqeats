package com.ffms.resqeats.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outlet item response DTO (item with outlet-specific details).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletItemDto {

    @JsonProperty("outlet_item_id")
    private Long outletItemId;

    @JsonProperty("outlet_id")
    private Long outletId;

    private ItemDto item;

    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

    @JsonProperty("real_time_quantity")
    private Integer realTimeQuantity;

    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @JsonProperty("pickup_start_time")
    private LocalDateTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalDateTime pickupEndTime;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_in_stock")
    public boolean isInStock() {
        int qty = realTimeQuantity != null ? realTimeQuantity : 
                  (quantityAvailable != null ? quantityAvailable : 0);
        return qty > 0;
    }
}
