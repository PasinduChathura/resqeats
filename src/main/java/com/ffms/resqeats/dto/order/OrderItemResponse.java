package com.ffms.resqeats.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("secret_box_id")
    private Long secretBoxId;

    @JsonProperty("secret_box_name")
    private String secretBoxName;

    @JsonProperty("secret_box_image_url")
    private String secretBoxImageUrl;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("original_value")
    private BigDecimal originalValue;

    @JsonProperty("savings")
    private BigDecimal savings;
}
