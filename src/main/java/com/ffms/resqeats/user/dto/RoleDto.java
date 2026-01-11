package com.ffms.resqeats.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private UserRole name;

    @JsonProperty("hierarchy_level")
    private int hierarchyLevel;

    private String displayName;

    private String description;
}
