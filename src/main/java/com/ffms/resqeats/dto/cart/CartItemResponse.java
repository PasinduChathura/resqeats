package com.ffms.resqeats.dto.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("secret_box_id")
    private Long secretBoxId;

    @JsonProperty("secret_box_name")
    private String secretBoxName;

    @JsonProperty("secret_box_image_url")
    private String secretBoxImageUrl;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_address")
    private String shopAddress;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;
}
