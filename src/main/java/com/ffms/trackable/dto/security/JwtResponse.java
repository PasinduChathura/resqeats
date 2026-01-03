package com.ffms.trackable.dto.security;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;

    private String refreshToken;

    private String type = "Bearer";

    private Long id;

    private String username;

    private String email;

    private List<String> privileges;

    private Integer role;

    public JwtResponse(String token, String refreshToken, Long id, String username, String email, List<String> privileges, Integer role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.privileges = privileges;
        this.role = role;
    }
}
