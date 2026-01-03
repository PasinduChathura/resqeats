package com.ffms.resqeats.security.oauth2;

import lombok.Getter;
import java.util.Map;

/**
 * Extracts user information from Google OAuth2 response.
 */
@Getter
public class GoogleOAuth2UserInfo {
    
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String imageUrl;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.firstName = (String) attributes.get("given_name");
        this.lastName = (String) attributes.get("family_name");
        this.imageUrl = (String) attributes.get("picture");
    }
}
