package com.ffms.trackable.security.oauth2;

import com.ffms.trackable.dto.security.JwtResponse;
import com.ffms.trackable.jwt.JwtUtils;
import com.ffms.trackable.models.security.RefreshToken;
import com.ffms.trackable.security.CustomUserDetails;
import com.ffms.trackable.service.security.RefreshTokenService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        try {
            // Generate JWT token for the authenticated user
            String token = jwtUtils.generateJwtToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // Get user privileges
            List<String> privileges = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Create refresh token for OAuth2 user
            RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(userDetails.getUsername());

            // Build complete JWT response matching the signin flow
            JwtResponse jwtResponse = new JwtResponse(
                    token,
                    refreshToken.getToken(),
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    privileges,
                    userDetails.getRole()
            );

            // Convert response to JSON and encode for URL
            Gson gson = new Gson();
            String authData = URLEncoder.encode(gson.toJson(jwtResponse), StandardCharsets.UTF_8);

            // Build the redirect URL with complete auth data
            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("authData", authData)
                    .queryParam("success", "true")
                    .build().toUriString();
        } catch (Exception e) {
            logger.error("Error generating OAuth2 authentication response", e);
            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "Authentication processing failed")
                    .build().toUriString();
        }
    }
}
