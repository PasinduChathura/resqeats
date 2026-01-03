package com.ffms.resqeats.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @JsonProperty("notes")
    private String notes;

    @NotNull(message = "Payment method ID is required")
    @JsonProperty("payment_method_id")
    private Long paymentMethodId;
}
