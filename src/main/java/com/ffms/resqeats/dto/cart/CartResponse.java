package com.ffms.resqeats.dto.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.cart.CartStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("status")
    private CartStatus status;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("total_items")
    private Integer totalItems;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("items")
    private List<CartItemResponse> items;

    @JsonProperty("is_expired")
    private Boolean isExpired;
}
