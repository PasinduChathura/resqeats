package com.ffms.resqeats.order.dto;

import com.ffms.resqeats.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Filter DTO for order list queries.
 * Supports comprehensive filtering for order management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order filter criteria")
public class OrderFilterDto {

    @Schema(description = "Filter by user ID")
    private Long userId;

    @Schema(description = "Filter by outlet ID")
    private Long outletId;

    @Schema(description = "Filter by merchant ID")
    private Long merchantId;

    @Schema(description = "Filter by order status (CREATED, PENDING_OUTLET_ACCEPTANCE, PAID, PREPARING, etc.)")
    private OrderStatus status;

    @Schema(description = "Filter by multiple order statuses")
    private List<OrderStatus> statuses;

    @Schema(description = "Search by order number")
    private String orderNumber;

    @Schema(description = "Filter orders created after this date")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter orders created before this date")
    private LocalDateTime dateTo;

    @Schema(description = "Filter orders by pickup time from")
    private LocalDateTime pickupTimeFrom;

    @Schema(description = "Filter orders by pickup time to")
    private LocalDateTime pickupTimeTo;

    @Schema(description = "Filter by minimum total amount")
    private BigDecimal minAmount;

    @Schema(description = "Filter by maximum total amount")
    private BigDecimal maxAmount;

    @Schema(description = "Filter by payment method ID")
    private Long paymentMethodId;

    @Schema(description = "Filter orders that are expired")
    private Boolean expired;

    @Schema(description = "Filter orders that are refunded")
    private Boolean refunded;

    @Schema(description = "Search in customer phone or email")
    private String customerSearch;
}
