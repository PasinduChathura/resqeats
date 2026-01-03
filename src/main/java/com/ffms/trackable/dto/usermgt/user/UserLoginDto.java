package com.ffms.trackable.dto.usermgt.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDto {
    @NotBlank(message = "is required")
    private String userName;

    @NotBlank(message = "is required")
    private String password;
}
