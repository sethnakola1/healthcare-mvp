package com.healthcare.mvp.config;// ============================================
// SWAGGER DOCUMENTATION & TESTING SETUP
// ============================================

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
//@EnableWebSecurity
@OpenAPIDefinition(
    info = @Info(
        title = "HealthHorizon MVP API",
        version = "1.0.0",
        description = "Comprehensive Healthcare Management System - MVP",
        contact = @Contact(
            name = "Sethna Kola",
            email = "sethna.kola@healthcareplatform.com"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth"),
    servers = {
        @Server(url = "http://localhost:8080/api", description = "Local Development Server"),
        @Server(url = "https://api.healthhorizon.com", description = "Production Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT token obtained from AWS Cognito authentication"
)
public class OpenApiConfig {
    
    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("business-management")
                .pathsToMatch("/api/business/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi hospitalApi() {
        return GroupedOpenApi.builder()
                .group("hospital-management")
                .pathsToMatch("/api/hospitals/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi medicalApi() {
        return GroupedOpenApi.builder()
                .group("medical-operations")
                .pathsToMatch("/api/patients/**", "/api/appointments/**", "/api/medical-records/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi billingApi() {
        return GroupedOpenApi.builder()
                .group("billing-operations")
                .pathsToMatch("/api/billing/**", "/api/payments/**")
                .build();
    }
}