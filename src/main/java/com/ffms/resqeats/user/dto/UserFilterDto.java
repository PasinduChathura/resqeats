package com.ffms.resqeats.user.dto;

import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Filter DTO for user list queries.
 * Supports comprehensive filtering for admin user management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User filter criteria")
public class UserFilterDto {

    @Schema(description = "Filter by user role (ADMIN, MERCHANT, OUTLET_USER, USER)")
    private UserRole role;

    @Schema(description = "Filter by user status (ACTIVE, SUSPENDED, DISABLED)")
    private UserStatus status;

    @Schema(description = "Search in email, phone, first name, or last name")
    private String search;

    @Schema(description = "Filter by associated merchant ID")
    private Long merchantId;

    @Schema(description = "Filter by associated outlet ID")
    private Long outletId;

    @Schema(description = "Filter users created after this date")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter users created before this date")
    private LocalDateTime dateTo;

    @Schema(description = "Filter by email domain (e.g., gmail.com)")
    private String emailDomain;

    @Schema(description = "Filter by verification status")
    private Boolean verified;
}
