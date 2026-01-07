package com.ffms.resqeats.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {

    @NotBlank(message = "Refresh token is required")
    private String token;
}
