package com.ffms.resqeats.merchant.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCustomerDto {

    private Long id;

    private String name;

    @JsonProperty("legal_name")
    private String legalName;

    private MerchantCategory category;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    private String description;

    private String website;
}
