package com.ffms.resqeats.merchant.dto;

import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Filter DTO for merchant list queries.
 * Supports comprehensive filtering for admin merchant management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Merchant filter criteria")
public class MerchantFilterDto {

    @Schema(description = "Filter by merchant status (PENDING, APPROVED, SUSPENDED, REJECTED)")
    private MerchantStatus status;

    @Schema(description = "Filter by merchant category (RESTAURANT, BAKERY, CAFE, GROCERY, OTHER)")
    private MerchantCategory category;

    @Schema(description = "Search in name, legal name, or description")
    private String search;

    @Schema(description = "Filter by registration number")
    private String registrationNo;

    @Schema(description = "Filter merchants created after this date")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter merchants created before this date")
    private LocalDateTime dateTo;

    @Schema(description = "Filter merchants approved after this date")
    private LocalDateTime approvedFrom;

    @Schema(description = "Filter merchants approved before this date")
    private LocalDateTime approvedTo;

    @Schema(description = "Filter by approver user ID")
    private Long approvedBy;

    @Schema(description = "Filter by city")
    private String city;

    @Schema(description = "Filter by contact email domain")
    private String emailDomain;
}
