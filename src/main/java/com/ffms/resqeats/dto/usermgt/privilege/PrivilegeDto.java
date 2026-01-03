package com.ffms.resqeats.dto.usermgt.privilege;

import com.ffms.resqeats.validation.common.annotations.NotBlankIfNotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeDto {
    private Long id;

    @NotBlank(message = "is required")
    private String name;

    @NotBlankIfNotNull
    private String description;
}
