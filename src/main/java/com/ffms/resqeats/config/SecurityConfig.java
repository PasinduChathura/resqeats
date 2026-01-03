package com.ffms.resqeats.config;

import com.ffms.resqeats.jwt.AuthEntryPointJwt;
import com.ffms.resqeats.jwt.AuthTokenFilter;
import com.ffms.resqeats.security.CustomUserDetailsService;
import com.ffms.resqeats.security.oauth2.CustomOAuth2UserService;
import com.ffms.resqeats.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.ffms.resqeats.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

/**
 * Centralized Security Configuration.
 * 
 * Role Hierarchy: SUPER_ADMIN > ADMIN > MERCHANT > OUTLET_USER > USER
 * 
 * Security Rules:
 * - Public endpoints: auth, oauth2, public item/outlet discovery
 * - SUPER_ADMIN: unrestricted access (audited)
 * - ADMIN: global access (audited)
 * - MERCHANT: merchant-scoped access
 * - OUTLET_USER: outlet-scoped access
 * - USER: user-scoped access
 * 
 * CRITICAL-003 FIX: Added SameSite cookie configuration for OAuth2 flows.
 * MEDIUM-008 FIX: Swagger UI protection based on profile.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    
    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    @Autowired
    private Environment environment;

    /**
     * Role hierarchy definition.
     * Higher roles inherit permissions from lower roles.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
            "ROLE_SUPER_ADMIN > ROLE_ADMIN\n" +
            "ROLE_ADMIN > ROLE_MERCHANT\n" +
            "ROLE_MERCHANT > ROLE_OUTLET_USER\n" +
            "ROLE_OUTLET_USER > ROLE_USER"
        );
        return hierarchy;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // BCrypt with strength 12
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Local SPA dev servers (Vite/CRA/etc). Add production origins via gateway/proxy as needed.
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        // CRITICAL-003 FIX: CSRF disabled is acceptable for stateless JWT API
        // SameSite cookie protection added via application.properties
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> {
                        // MEDIUM-008 FIX: Swagger UI only accessible in non-prod environments
                        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
                        
                        auth.requestMatchers(
                                // Auth endpoints (public)
                                "/auth/signin", "/auth/signup", "/auth/forgot-password",
                                "/auth/reset-password", "/auth/refresh-token", "/auth/otp/**", "/auth/register",
                                "/oauth2/**", "/login/oauth2/**", "/test/*",
                                // WebSocket endpoints
                                "/ws/**", "/ws-native/**",
                                // Public API endpoints (outlets and items discovery per SRS)
                                "/outlets/nearby", "/outlets/search", "/outlets/{id}",
                                "/items/nearby", "/items/{id}", "/outlets/{outletId}/items",
                                "/outlets/{outletId}/items/available",
                                // Payment webhook endpoint (called by payment gateway)
                                "/payments/webhook",
                                // Health check
                                "/actuator/health"
                        ).permitAll();
                        
                        // MEDIUM-008 FIX: Swagger/OpenAPI documentation - deny in production
                        if (isProduction) {
                            auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml")
                                .denyAll();
                        } else {
                            auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml")
                                .permitAll();
                        }
                                
                        // SUPER_ADMIN only endpoints
                        auth.requestMatchers("/admin/super/**").hasRole("SUPER_ADMIN")
                                
                                // ADMIN or higher endpoints
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                
                                // Merchant management (ADMIN or MERCHANT)
                                .requestMatchers("/merchants/**").hasRole("MERCHANT")
                                
                                // Outlet management (ADMIN, MERCHANT, or OUTLET_USER)
                                .requestMatchers("/outlet/**").hasRole("OUTLET_USER")
                                
                                // User endpoints (all authenticated users)
                                .requestMatchers("/users/me/**").authenticated()
                                .requestMatchers("/users/**").hasRole("ADMIN")
                                
                                // Cart and order endpoints (authenticated users)
                                .requestMatchers("/cart/**", "/orders/**").authenticated()
                                
                                // All other requests require authentication
                                .anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
