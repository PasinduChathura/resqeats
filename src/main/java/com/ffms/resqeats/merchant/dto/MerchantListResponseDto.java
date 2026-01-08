package com.ffms.resqeats.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Merchant list response DTO with essential fields for table display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantListResponseDto {

    private UUID id;

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

    private MerchantStatus status;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Additional fields for list display
    @JsonProperty("owner_user_id")
    private UUID ownerUserId;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("owner_email")
    private String ownerEmail;

    @JsonProperty("outlet_count")
    private Integer outletCount;
}
