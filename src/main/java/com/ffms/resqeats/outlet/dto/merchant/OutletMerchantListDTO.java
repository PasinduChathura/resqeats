package com.ffms.resqeats.outlet.dto.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletMerchantListDTO {
	private Long id;
	private String name;
	private String phone;
	private OutletStatus status;
	private String address;

	@JsonProperty("item_count")
	private Long itemCount;

	@JsonProperty("is_open")
	private Boolean isOpen;

	@JsonProperty("average_rating")
	private BigDecimal averageRating;
}
