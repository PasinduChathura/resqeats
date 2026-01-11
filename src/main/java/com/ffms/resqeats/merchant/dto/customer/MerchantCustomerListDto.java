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
public class MerchantCustomerListDto {

    private Long id;

    private String name;

    private MerchantCategory category;

    @JsonProperty("logo_url")
    private String logoUrl;

    private String description;
}
