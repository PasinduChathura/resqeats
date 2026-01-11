package com.ffms.resqeats.outlet.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outlet lookup DTO with minimal fields for dropdown/autocomplete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletLookupDto {

    private Long id;

    private String name;
}
