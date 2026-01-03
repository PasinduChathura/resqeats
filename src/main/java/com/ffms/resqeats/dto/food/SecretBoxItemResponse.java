package com.ffms.resqeats.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretBoxItemResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("food_item_id")
    private Long foodItemId;

    @JsonProperty("food_item_name")
    private String foodItemName;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("notes")
    private String notes;
}
