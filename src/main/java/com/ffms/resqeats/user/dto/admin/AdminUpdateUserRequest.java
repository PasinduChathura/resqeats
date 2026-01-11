package com.ffms.resqeats.user.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin update user request DTO.
 * Used by ADMIN to update any user's details.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone number too long")
    private String phone;

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
     * Change user role (with proper validation).
     * Cannot escalate to higher role than the admin's own role.
     */
    private UserRole role;

    /**
     * Change user status directly.
     */
    private UserStatus status;

    /**
     * Reassign merchant (for MERCHANT_USER or OUTLET_USER).
     */
    @JsonProperty("merchant_id")
    private Long merchantId;

    /**
     * Reassign outlet (for OUTLET_USER).
     */
    @JsonProperty("outlet_id")
    private Long outletId;

    /**
     * Set email verified status.
     */
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    /**
     * Set phone verified status.
     */
    @JsonProperty("phone_verified")
    private Boolean phoneVerified;

    /**
     * Reset password to a new value.
     * If provided, password will be changed.
     */
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;

    /**
     * Send password reset email instead of setting password directly.
     */
    @JsonProperty("send_password_reset")
    private Boolean sendPasswordReset;
}
