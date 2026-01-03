package com.ffms.resqeats.dto.security;

import com.ffms.resqeats.validation.usermgt.annotations.PasswordMatches;
import lombok.*;

@PasswordMatches
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {

    private String token;
}
