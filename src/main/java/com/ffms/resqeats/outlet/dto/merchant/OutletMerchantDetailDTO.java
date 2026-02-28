package com.ffms.resqeats.outlet.dto.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import com.ffms.resqeats.outlet.enums.OutletAvailabilityStatus;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletMerchantDetailDTO {
	private Long id;

	private String name;
	private String description;

	private String address;
	private String city;
	private BigDecimal latitude;
	private BigDecimal longitude;

	private String phone;
	private OutletStatus status;

	@JsonProperty("availability_status")
	private OutletAvailabilityStatus availabilityStatus;

	@JsonProperty("postal_code")
	private String postalCode;

	@JsonProperty("is_open")
	private Boolean isOpen;

	@JsonProperty("average_rating")
	private BigDecimal averageRating;

	@JsonProperty("total_ratings")
	private Integer totalRatings;

	@JsonProperty("operating_hours")
	private List<OperatingHoursDto> operatingHours;
}
