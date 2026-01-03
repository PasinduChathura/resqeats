package com.ffms.resqeats.dto.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatingDayRequest {

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @JsonProperty("opening_time")
    private LocalTime openingTime;

    @JsonProperty("closing_time")
    private LocalTime closingTime;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @JsonProperty("is_closed")
    @Builder.Default
    private Boolean isClosed = false;
}
