package com.ffms.resqeats.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outlet association DTO for OUTLET_USER role users.
 * Contains outlet details associated with the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletAssociation {

    @JsonProperty("outlet_id")
    private Long outletId;

    @JsonProperty("outlet_name")
    private String outletName;

    @JsonProperty("outlet_address")
    private String outletAddress;

    @JsonProperty("outlet_city")
    private String outletCity;

    @JsonProperty("merchant_id")
    private Long merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;
}
