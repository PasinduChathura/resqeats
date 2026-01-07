package com.ffms.resqeats.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create merchant request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMerchantRequest {

    @NotBlank(message = "Merchant name is required")
    private String name;

    @JsonProperty("legal_name")
    private String legalName;

    private String description;

    @NotNull(message = "Category is required")
    private MerchantCategory category;

    @JsonProperty("registration_no")
    private String registrationNo;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("contact_email")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @JsonProperty("contact_phone")
    private String contactPhone;

    private String website;

    @JsonProperty("logo_url")
    private String logoUrl;
}
