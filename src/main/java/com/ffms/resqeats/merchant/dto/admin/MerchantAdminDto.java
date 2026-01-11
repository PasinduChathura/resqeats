package com.ffms.resqeats.merchant.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAdminDto {

    private Long id;

    private String name;

    @JsonProperty("legal_name")
    private String legalName;

    @JsonProperty("registration_no")
    private String registrationNo;

    private MerchantCategory category;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    private String description;

    private String website;

    private MerchantStatus status;

//    @JsonProperty("rejection_reason")
//    private String rejectionReason;
//
//    @JsonProperty("suspension_reason")
//    private String suspendedReason;
}
