package com.ffms.resqeats.dto.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {

    @NotNull(message = "Secret box ID is required")
    @JsonProperty("secret_box_id")
    private Long secretBoxId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    @Builder.Default
    private Integer quantity = 1;
}
