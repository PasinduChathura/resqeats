package com.ffms.resqeats.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderActionRequest {

    @JsonProperty("reason")
    private String reason;
}
