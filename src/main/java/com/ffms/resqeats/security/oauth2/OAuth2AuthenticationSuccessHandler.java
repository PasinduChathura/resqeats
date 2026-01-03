package com.ffms.resqeats.security.oauth2;

import com.ffms.resqeats.dto.security.JwtResponse;
import com.ffms.resqeats.jwt.JwtUtils;
import com.ffms.resqeats.models.security.RefreshToken;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.security.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles successful Google OAuth2 authentication.
 * Generates JWT tokens and redirects to frontend with auth data.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = buildRedirectUrl(authentication);

        if (response.isCommitted()) {
            log.warn("Response already committed. Unable to redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String buildRedirectUrl(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // Generate JWT token
            String accessToken = jwtUtils.generateJwtToken(authentication);
            
            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(userDetails.getUsername());

            // Get user authorities
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // Build JWT response
            JwtResponse jwtResponse = new JwtResponse(
                    accessToken,
                    refreshToken.getToken(),
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    authorities,
                    userDetails.getRole()
            );

            // Encode response as JSON for URL
            String authData = URLEncoder.encode(objectMapper.writeValueAsString(jwtResponse), StandardCharsets.UTF_8);

            log.info("Google OAuth2 login successful for user: {}", userDetails.getUsername());

            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("authData", authData)
                    .queryParam("success", "true")
                    .build().toUriString();

        } catch (Exception e) {
            log.error("Error generating OAuth2 response", e);
            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .queryParam("success", "false")
                    .build().toUriString();
        }
    }
}
