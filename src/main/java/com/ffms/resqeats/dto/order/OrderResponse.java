package com.ffms.resqeats.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.order.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_phone")
    private String userPhone;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_address")
    private String shopAddress;

    @JsonProperty("shop_phone")
    private String shopPhone;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    @JsonProperty("service_fee")
    private BigDecimal serviceFee;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("pickup_code")
    private String pickupCode;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @JsonProperty("pickup_deadline")
    private LocalDateTime pickupDeadline;

    @JsonProperty("shop_acceptance_deadline")
    private LocalDateTime shopAcceptanceDeadline;

    @JsonProperty("accepted_at")
    private LocalDateTime acceptedAt;

    @JsonProperty("declined_at")
    private LocalDateTime declinedAt;

    @JsonProperty("decline_reason")
    private String declineReason;

    @JsonProperty("preparing_at")
    private LocalDateTime preparingAt;

    @JsonProperty("ready_at")
    private LocalDateTime readyAt;

    @JsonProperty("picked_up_at")
    private LocalDateTime pickedUpAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("user_rating")
    private Integer userRating;

    @JsonProperty("user_feedback")
    private String userFeedback;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("items")
    private List<OrderItemResponse> items;
}
