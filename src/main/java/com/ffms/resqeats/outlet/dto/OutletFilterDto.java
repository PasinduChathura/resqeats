package com.ffms.resqeats.outlet.dto;

import com.ffms.resqeats.outlet.enums.OutletStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filter DTO for outlet list queries.
 * Supports comprehensive filtering including location-based searches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Outlet filter criteria")
public class OutletFilterDto {

    @Schema(description = "Filter by merchant ID")
    private Long merchantId;

    @Schema(description = "Filter by outlet status (PENDING_APPROVAL, ACTIVE, SUSPENDED, DISABLED)")
    private OutletStatus status;

    @Schema(description = "Filter by city")
    private String city;

    @Schema(description = "Search in outlet name, address, or description")
    private String search;

    @Schema(description = "Center latitude for radius search")
    private BigDecimal latitude;

    @Schema(description = "Center longitude for radius search")
    private BigDecimal longitude;

    @Schema(description = "Search radius in kilometers (used with latitude/longitude)")
    private BigDecimal radiusKm;

    @Schema(description = "Filter outlets created after this date")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter outlets created before this date")
    private LocalDateTime dateTo;

    @Schema(description = "Filter by postal code")
    private String postalCode;

    @Schema(description = "Filter by minimum rating")
    private BigDecimal minRating;

    @Schema(description = "Filter outlets that are currently open")
    private Boolean openNow;
}
