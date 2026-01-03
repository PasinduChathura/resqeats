package com.ffms.trackable.dto.usermgt.role;

import com.ffms.trackable.validation.common.annotations.NotBlankIfNotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto {
    private Long id;

    @NotBlankIfNotNull
    @Size(min = 1, max = 100, message = "must have between {min} and {max} characters")
    private String description;
}
