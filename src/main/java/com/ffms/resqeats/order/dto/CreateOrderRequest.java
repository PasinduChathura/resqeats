package com.ffms.resqeats.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Create order request DTO.
 * 
 * LOW FIX (Issue #17): Added @Valid for nested object validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Outlet ID is required")
    @JsonProperty("outlet_id")
    private UUID outletId;

    @NotEmpty(message = "Order items are required")
    @Valid  // LOW FIX: Enables validation of nested OrderItemRequest objects
    private List<OrderItemRequest> items;

    @JsonProperty("pickup_by")
    private LocalDateTime pickupBy;

    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Item ID is required")
        @JsonProperty("item_id")
        private UUID itemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @JsonProperty("unit_price")
        private BigDecimal unitPrice;

        @JsonProperty("item_name")
        private String itemName;
    }
}
