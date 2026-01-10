package com.ffms.resqeats.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Merchant response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDto {

    private Long id;

    private String name;

    @JsonProperty("legal_name")
    private String legalName;

    private String description;

    private MerchantCategory category;

    @JsonProperty("registration_no")
    private String registrationNo;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    private String website;

    @JsonProperty("logo_url")
    private String logoUrl;

    private MerchantStatus status;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
