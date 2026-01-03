package com.ffms.resqeats.dto.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyShopsRequest {

    @NotNull(message = "Latitude is required")
    @JsonProperty("latitude")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("radius_km")
    @Builder.Default
    private Double radiusKm = 5.0; // Default 5km radius

    @JsonProperty("category")
    private String category;

    @JsonProperty("page")
    @Builder.Default
    private Integer page = 0;

    @JsonProperty("size")
    @Builder.Default
    private Integer size = 20;
}
