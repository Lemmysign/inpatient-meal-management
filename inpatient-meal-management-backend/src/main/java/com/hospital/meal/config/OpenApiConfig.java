package com.hospital.meal.config;

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

@Configuration
public class OpenApiConfig {

    @Value("${server.port:9096}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Meal Ordering System API")
                        .version("1.0.0")
                        .description("""
                    Production-grade backend system for hospital meal ordering and diet management.
                    
                    ## Features
                    - Patient meal ordering (no password authentication)
                    - Dietician menu management
                    - Kitchen staff meal processing
                    - Admin user management
                    - Real-time notifications
                    - Audit logging
                    
                    ## Roles
                    - **Admin**: Create and manage users, view system-wide metrics
                    - **Dietician**: Create menus, assign to patients, view order history
                    - **Kitchen Staff**: Process meals, print labels, view queues
                    - **Patient**: Order meals, view menu, modify orders (time-based)
                    """)
                        .contact(new Contact()
                                .name("Hospital IT Team")
                                .email("support@hospital.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://hospital.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.hospital.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login endpoint")
                        )
                );
    }
}