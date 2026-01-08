package com.ffms.resqeats.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * User entity per SRS Section 7.2.
 * Supports phone/OTP, email/password, and OAuth2 authentication.
 * Enforces RBAC: ADMIN, MERCHANT, OUTLET_USER, USER.
 * 
 * HIBERNATE FILTERS (applied at repository level via TenantFilterAspect):
 * - userMerchantFilter: MERCHANT role sees users within their merchant
 * - userOutletFilter: OUTLET_USER role sees users within their outlet
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "phone"),
        @UniqueConstraint(columnNames = "email")
})
@FilterDefs({
    @FilterDef(name = "userMerchantFilter", parameters = @ParamDef(name = "merchantId", type = String.class)),
    @FilterDef(name = "userOutletFilter", parameters = @ParamDef(name = "outletId", type = String.class))
})
@Filters({
    @Filter(name = "userMerchantFilter", condition = "merchant_id = :merchantId"),
    @Filter(name = "userOutletFilter", condition = "outlet_id = :outletId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "phone", length = 20, unique = true)
    @JsonProperty("phone")
    private String phone;

    @Email
    @Column(name = "email", length = 255, unique = true)
    @JsonProperty("email")
    private String email;

    @JsonIgnore
    @Size(max = 255)
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @JsonProperty("role")
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "first_name", length = 100)
    @JsonProperty("first_name")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @JsonProperty("last_name")
    private String lastName;

    @Column(name = "profile_image_url", length = 500)
    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Associated merchant for MERCHANT role users.
     * Per SRS: MERCHANT users can only access their own merchant's outlets.
     */
    @Column(name = "merchant_id")
    @JsonProperty("merchant_id")
    private UUID merchantId;

    /**
     * Associated outlet for OUTLET_USER role users.
     * Per SRS: OUTLET_USER can only access their assigned outlet.
     */
    @Column(name = "outlet_id")
    @JsonProperty("outlet_id")
    private UUID outletId;

    // OAuth2 fields
    @Column(name = "oauth2_provider", length = 30)
    @JsonProperty("oauth2_provider")
    private String oauth2Provider;

    @Column(name = "oauth2_provider_id", length = 255)
    @JsonProperty("oauth2_provider_id")
    private String oauth2ProviderId;

    // Phone OTP verification
    @Column(name = "phone_verified")
    @JsonProperty("phone_verified")
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "email_verified")
    @JsonProperty("email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    // Notification preferences
    @Column(name = "push_notifications_enabled")
    @JsonProperty("push_notifications_enabled")
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

    @Column(name = "fcm_token", length = 500)
    @JsonProperty("fcm_token")
    private String fcmToken;

    @Column(name = "apns_token", length = 500)
    @JsonProperty("apns_token")
    private String apnsToken;

    /**
     * Get full name for display purposes.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email != null ? email : phone;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    /**
     * Check if user is a super administrator.
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    /**
     * Check if user is a platform administrator.
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }

    /**
     * Check if user is a merchant owner.
     */
    public boolean isMerchant() {
        return role == UserRole.MERCHANT;
    }

    /**
     * Check if user is an outlet staff member.
     */
    public boolean isOutletUser() {
        return role == UserRole.OUTLET_USER;
    }

    /**
     * Check if user is a regular customer.
     */
    public boolean isCustomer() {
        return role == UserRole.USER;
    }

    /**
     * Check if user has global access (no tenant filtering).
     */
    public boolean hasGlobalAccess() {
        return role != null && role.hasGlobalAccess();
    }

    /**
     * Check if user has at least the specified role.
     */
    public boolean hasRole(UserRole minimumRole) {
        return role != null && role.isAtLeast(minimumRole);
    }
}
