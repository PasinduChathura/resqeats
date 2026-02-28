package com.ffms.resqeats.outlet.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.enums.OutletAvailabilityStatus;
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
public class OutletAdminListDTO {
	private Long id;
	private String name;
	private String phone;

	@JsonProperty("merchant_name")
	private String merchantName;

	@JsonProperty("merchant_logo_url")
	private String merchantLogoUrl;

	private OutletStatus status;

	@JsonProperty("availability_status")
	private OutletAvailabilityStatus availabilityStatus;

	private String address;

	@JsonProperty("item_count")
	private Long itemCount;

	@JsonProperty("is_open")
	private Boolean isOpen;

	@JsonProperty("average_rating")
	private BigDecimal averageRating;
}
