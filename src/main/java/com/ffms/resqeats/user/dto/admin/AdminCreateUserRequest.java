package com.ffms.resqeats.user.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin create user request DTO.
 * Used by ADMIN to create users of any role.
 * <p>
 * Validation rules:
 * - ADMIN/SUPER_ADMIN: Only SUPER_ADMIN can create these
 * - MERCHANT_USER: merchantId required, outletId prohibited
 * - OUTLET_USER: outletId required, merchantId prohibited
 * - CUSTOMER_USER: No association required
 * <p>
 * Password handling:
 * - If password is provided, it will be hashed and saved
 * - If password is null, a temporary password will be generated and emailed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone number too long")
    private String phone;

    /**
     * Optional password. If null, a temporary password will be generated
     * and sent to the user's email address.
     */
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;

    @JsonProperty("first_name")
    @Size(max = 100, message = "First name too long")
    private String firstName;

    @JsonProperty("last_name")
    @Size(max = 100, message = "Last name too long")
    private String lastName;

    @JsonProperty("profile_image_url")
    @Size(max = 500, message = "Profile image URL too long")
    private String profileImageUrl;

    /**
     * Role for the new user.
     * SUPER_ADMIN can create any role.
     * ADMIN can create MERCHANT_USER, OUTLET_USER, CUSTOMER_USER.
     */
    @NotNull(message = "Role is required")
    private UserRole role;

    /**
     * Merchant ID for MERCHANT_USER only.
     * Required when role is MERCHANT_USER.
     * Prohibited when role is OUTLET_USER.
     */
    @JsonProperty("merchant_id")
    private Long merchantId;

    /**
     * Outlet ID for OUTLET_USER only.
     * Required when role is OUTLET_USER.
     * Prohibited when role is MERCHANT_USER.
     */
    @JsonProperty("outlet_id")
    private Long outletId;

    /**
     * Whether to send welcome email with credentials.
     * Defaults to true if password is auto-generated.
     */
    @JsonProperty("send_welcome_email")
    @Builder.Default
    private Boolean sendWelcomeEmail = true;
}
