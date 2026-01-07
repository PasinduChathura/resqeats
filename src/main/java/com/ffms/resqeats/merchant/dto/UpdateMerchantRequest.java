package com.ffms.resqeats.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update merchant request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMerchantRequest {

    private String name;

    private String description;

    private String contactEmail;

    private String contactPhone;

    private String website;

    private String logoUrl;
}
