package com.ffms.resqeats.payment.dto;

import com.ffms.resqeats.payment.enums.PaymentMethodType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private PaymentMethodType paymentMethodType;
    private String cardLastFour;
    private String cardBrand;
    private Integer cardExpiryMonth;
    private Integer cardExpiryYear;
    private String cardHolderName;
    private Boolean isDefault;
    private String nickname;
}
