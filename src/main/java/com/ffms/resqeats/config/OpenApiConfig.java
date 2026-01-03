package com.ffms.resqeats.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Resqeats API documentation.
 * Accessible at /swagger-ui.html or /swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI resqeatsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resqeats API")
                        .description("Food Rescue Platform API - Connects consumers with discounted surplus food from local shops")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Resqeats Support")
                                .email("support@resqeats.com")
                                .url("https://resqeats.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://resqeats.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.resqeats.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /auth/signin or /auth/refreshToken")));
    }
}
