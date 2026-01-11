package com.ffms.resqeats.user.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full user DTO for admin operations.
 * Contains all user fields including sensitive admin-only information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDto {

    private Long id;

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

    @JsonProperty("merchant_id")
    private Long merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("outlet_id")
    private Long outletId;

    @JsonProperty("outlet_name")
    private String outletName;

    @JsonProperty("oauth2_provider")
    private String oauth2Provider;

    @JsonProperty("push_notifications_enabled")
    private Boolean pushNotificationsEnabled;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;
}
