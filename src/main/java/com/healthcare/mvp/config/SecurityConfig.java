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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * ENHANCED Security configuration with proper JWT authentication and method-level authorization
 *
 * This configuration provides:
 * - JWT-based authentication
 * - Method-level security with @PreAuthorize
 * - Proper CORS configuration
 * - Custom authentication and authorization error handling
 * - Token blocklist support (future enhancement)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - No authentication required
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/health",
                    "/api/auth/register",
                    "/api/auth/registration/**",
                    "/api/auth/reset-password",
                    "/api/auth/confirm-reset",
                    "/api/business/super-admin/initialize",
                    "/api/debug/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                // Business endpoints
                .requestMatchers("/api/business/**").hasAnyRole("SUPER_ADMIN", "TECH_ADVISOR")

                // Hospital endpoints
                .requestMatchers("/api/hospitals/**").hasAnyRole("SUPER_ADMIN", "TECH_ADVISOR", "HOSPITAL_ADMIN")

                // Medical endpoints
                .requestMatchers("/api/patients/**", "/api/appointments/**", "/api/medical-records/**")
                    .hasAnyRole("HOSPITAL_ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST", "PATIENT")

                // Billing endpoints
                .requestMatchers("/api/billing/**").hasAnyRole("HOSPITAL_ADMIN", "BILLING_STAFF", "RECEPTIONIST")

                // Doctor management endpoints
                .requestMatchers("/api/doctors/**").hasAnyRole("HOSPITAL_ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                // Prescription endpoints
                .requestMatchers("/api/prescriptions/**").hasAnyRole("DOCTOR", "HOSPITAL_ADMIN", "PATIENT")

                // User profile endpoints
                .requestMatchers("/api/users/**").authenticated()

                // Dashboard endpoints
                .requestMatchers("/api/dashboard/**").authenticated()

                // All other requests need authentication
                .anyRequest().authenticated()
            )

            // Custom exception handling
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

            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins for development - CHANGE FOR PRODUCTION
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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