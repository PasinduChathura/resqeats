package com.ffms.resqeats.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.usermgt.UserType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("user_type")
    private UserType userType;

    @JsonProperty("oauth2_provider")
    private String oauth2Provider;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;
}
