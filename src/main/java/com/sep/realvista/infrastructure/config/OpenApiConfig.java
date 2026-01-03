package com.sep.realvista.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration.
 * Configures OpenAPI 3.0 specification for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        String apiDescription = "RESTful API for RealVista Real Estate Platform - "
                + "Communication & Appointment Scheduling";
        String jwtDescription = "Enter JWT token obtained from /api/v1/auth/login endpoint";

        return new OpenAPI()
                .openapi("3.0.1") // Explicitly set OpenAPI version
                .info(new Info()
                        .title("RealVista API")
                        .version("1.0.0")
                        .description(apiDescription)
                        .contact(new Contact()
                                .name("RealVista Team")
                                .email("contact@realvista.com")
                                .url("https://realvista.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Development Server"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("Authorization")
                                        .description(jwtDescription)));
    }
}

