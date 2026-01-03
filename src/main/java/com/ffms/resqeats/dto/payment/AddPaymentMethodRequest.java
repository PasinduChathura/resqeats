package com.ffms.resqeats.dto.payment;

import com.ffms.resqeats.enums.payment.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPaymentMethodRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Card token is required")
    private String cardToken;

    @NotBlank(message = "Card last four digits is required")
    private String cardLastFour;

    @NotBlank(message = "Card brand is required")
    private String cardBrand;

    @NotNull(message = "Card expiry month is required")
    private Integer cardExpiryMonth;

    @NotNull(message = "Card expiry year is required")
    private Integer cardExpiryYear;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    private Boolean isDefault = false;

    private String nickname;
}
