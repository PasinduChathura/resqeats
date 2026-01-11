package com.ffms.resqeats.outlet.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletCustomerDTO {
	private String name;
	private String phone;
	private String address;

	@JsonProperty("merchant_name")
	private String merchantName;

	@JsonProperty("merchant_logo_url")
	private String merchantLogoUrl;

	@JsonProperty("is_open")
	private Boolean isOpen;

	@JsonProperty("average_rating")
	private BigDecimal averageRating;
}
