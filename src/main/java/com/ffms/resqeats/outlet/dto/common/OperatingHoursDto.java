package com.ffms.resqeats.outlet.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.DayOfWeek;

/**
 * Operating hours DTO used by outlet detail responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHoursDto {

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;
    @JsonProperty("open_time")
    private LocalTime openTime;

    @JsonProperty("close_time")
    private LocalTime closeTime;

    @JsonProperty("is_closed")
    private Boolean isClosed;
}
