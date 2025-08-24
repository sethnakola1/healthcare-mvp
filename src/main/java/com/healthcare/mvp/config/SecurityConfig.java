package com.healthcare.mvp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;

/**
 * SIMPLIFIED Security configuration with method-level authorization
 *
 * This version removes all OAuth2/JWT dependencies to avoid compilation issues
 * Perfect for MVP development and testing
 *
 * TODO: Add proper JWT/OAuth2 authentication for production
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource; // Injected from CorsConfig

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Use injected source
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/health",
                    "/api/auth/register",
                    "/api/auth/registration/**",
                    "/api/business/super-admin/initialize",
                    "/api/admin/users/reset-password",
                    "/api/debug/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()

                // Business endpoints
                .requestMatchers("/api/business/**").hasAnyRole("SUPER_ADMIN", "TECH_ADVISOR")

                // Hospital endpoints
                .requestMatchers("/api/hospitals/**").hasAnyRole("SUPER_ADMIN", "TECH_ADVISOR", "HOSPITAL_ADMIN")

                // Medical endpoints
                .requestMatchers("/api/patients/**", "/api/appointments/**", "/api/medical-records/**")
                    .hasAnyRole("HOSPITAL_ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                // Billing endpoints
                .requestMatchers("/api/billing/**").hasAnyRole("HOSPITAL_ADMIN", "BILLING_STAFF", "RECEPTIONIST")

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    BaseResponse<Object> errorResponse = BaseResponse.builder()
                            .success(false)
                            .errorCode("AUTHENTICATION_REQUIRED")
                            .message("Authentication required to access this resource")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .build();

                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    BaseResponse<Object> errorResponse = BaseResponse.builder()
                            .success(false)
                            .errorCode("ACCESS_DENIED")
                            .message("You don't have permission to access this resource")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .build();

                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                })
            )
            .headers(headers -> headers
                .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';"))
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(withDefaults())
                // .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true).preload(true)) // Enable this in production when HTTPS is enforced
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}