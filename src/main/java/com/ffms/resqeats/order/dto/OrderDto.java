package com.ffms.resqeats.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private UUID id;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("outlet_id")
    private UUID outletId;

    @JsonProperty("outlet_name")
    private String outletName;

    private OrderStatus status;

    @JsonProperty("status_display")
    private String statusDisplay;

    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal total;

    @JsonProperty("pickup_code")
    private String pickupCode;

    @JsonProperty("pickup_by")
    private LocalDateTime pickupBy;

    private String notes;

    private List<OrderItemDto> items;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @JsonProperty("ready_at")
    private LocalDateTime readyAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        @JsonProperty("item_id")
        private UUID itemId;

        @JsonProperty("item_name")
        private String itemName;

        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unitPrice;

        @JsonProperty("line_total")
        private BigDecimal lineTotal;
    }
}
