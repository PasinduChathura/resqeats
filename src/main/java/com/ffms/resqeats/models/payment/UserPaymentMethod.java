package com.ffms.resqeats.models.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.payment.PaymentMethod;
import com.ffms.resqeats.models.usermgt.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPaymentMethod extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", columnDefinition = "VARCHAR(30)")
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "card_token", columnDefinition = "VARCHAR(255)")
    @JsonProperty("card_token")
    private String cardToken;

    @Column(name = "card_last_four", columnDefinition = "VARCHAR(4)")
    @JsonProperty("card_last_four")
    private String cardLastFour;

    @Column(name = "card_brand", columnDefinition = "VARCHAR(30)")
    @JsonProperty("card_brand")
    private String cardBrand;

    @Column(name = "card_expiry_month")
    @JsonProperty("card_expiry_month")
    private Integer cardExpiryMonth;

    @Column(name = "card_expiry_year")
    @JsonProperty("card_expiry_year")
    private Integer cardExpiryYear;

    @Column(name = "card_holder_name", columnDefinition = "VARCHAR(150)")
    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @Column(name = "is_default")
    @JsonProperty("is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "nickname", columnDefinition = "VARCHAR(50)")
    @JsonProperty("nickname")
    private String nickname;
}
