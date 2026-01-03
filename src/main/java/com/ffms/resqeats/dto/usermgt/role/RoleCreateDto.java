package com.ffms.resqeats.dto.usermgt.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleCreateDto extends RoleDto {
    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "have between {min} and {max} characters")
    private String name;

    @NotEmpty(message = "must not be empty")
    private List<@Pattern(regexp = "\\d+", message = "privilege id must be a valid number") String> privileges;
}
