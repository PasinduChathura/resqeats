package com.ffms.trackable.dto.security;

import com.ffms.trackable.validation.usermgt.annotations.PasswordMatches;
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
