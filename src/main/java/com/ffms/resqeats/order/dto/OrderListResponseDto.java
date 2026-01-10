package com.ffms.resqeats.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order list response DTO with essential fields and association data for table display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponseDto {

    private Long id;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("outlet_id")
    private Long outletId;

    private OrderStatus status;

    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal total;

    @JsonProperty("pickup_code")
    private String pickupCode;

    @JsonProperty("pickup_by")
    private LocalDateTime pickupBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @JsonProperty("ready_at")
    private LocalDateTime readyAt;

    @JsonProperty("picked_up_at")
    private LocalDateTime pickedUpAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    private Integer rating;

    // User association data
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_phone")
    private String userPhone;

    // Outlet association data
    @JsonProperty("outlet_name")
    private String outletName;

    @JsonProperty("outlet_address")
    private String outletAddress;

    @JsonProperty("merchant_id")
    private Long merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;

    // Order items summary
    @JsonProperty("items_count")
    private Integer itemsCount;
}
