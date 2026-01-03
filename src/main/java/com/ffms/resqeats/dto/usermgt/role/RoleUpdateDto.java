package com.ffms.resqeats.dto.usermgt.role;

import com.ffms.resqeats.validation.common.annotations.NotBlankIfNotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleUpdateDto extends RoleDto {
    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "have between {min} and {max} characters")
    private String name;

    private List<@Pattern(regexp = "\\d+", message = "privilege id must be a valid number") String> privileges;
}
