package com.ffms.resqeats.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Merchant association DTO for MERCHANT_USER role users.
 * Contains merchant details associated with the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAssociation {

    @JsonProperty("merchant_id")
    private Long merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("merchant_logo_url")
    private String merchantLogoUrl;

    @JsonProperty("merchant_contact_email")
    private String merchantContactEmail;

    @JsonProperty("merchant_contact_phone")
    private String merchantContactPhone;
}
