package com.ffms.resqeats.dto.payment;

import com.ffms.resqeats.enums.payment.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private PaymentMethod paymentMethod;
    private String cardLastFour;
    private String cardBrand;
    private Integer cardExpiryMonth;
    private Integer cardExpiryYear;
    private String cardHolderName;
    private Boolean isDefault;
    private String nickname;
}
