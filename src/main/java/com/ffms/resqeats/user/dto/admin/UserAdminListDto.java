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
 * Compact user DTO for admin list/table views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminListDto {

    private Long id;

    private String email;

    private String phone;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private UserRole role;

    private UserStatus status;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("outlet_name")
    private String outletName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
