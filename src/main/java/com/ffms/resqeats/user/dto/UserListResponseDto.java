package com.ffms.resqeats.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User list response DTO with association data for table display.
 * Includes merchant or outlet association information based on user role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDto {

    private UUID id;

    private String email;

    private String phone;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    private UserRole role;

    private UserStatus status;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("phone_verified")
    private Boolean phoneVerified;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Association data for MERCHANT role users
    @JsonProperty("merchant_association")
    private MerchantAssociation merchantAssociation;

    // Association data for OUTLET_USER role users
    @JsonProperty("outlet_association")
    private OutletAssociation outletAssociation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantAssociation {
        @JsonProperty("merchant_id")
        private UUID merchantId;

        @JsonProperty("merchant_name")
        private String merchantName;

        @JsonProperty("merchant_logo_url")
        private String merchantLogoUrl;

        @JsonProperty("merchant_contact_email")
        private String merchantContactEmail;

        @JsonProperty("merchant_contact_phone")
        private String merchantContactPhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutletAssociation {
        @JsonProperty("outlet_id")
        private UUID outletId;

        @JsonProperty("outlet_name")
        private String outletName;

        @JsonProperty("outlet_address")
        private String outletAddress;

        @JsonProperty("outlet_city")
        private String outletCity;

        @JsonProperty("merchant_id")
        private UUID merchantId;

        @JsonProperty("merchant_name")
        private String merchantName;
    }
}
