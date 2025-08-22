// JpaConfig.java
package com.healthcare.mvp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.mvp.shared.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Configuration for auditing and JSON processing
 */
@Configuration
//@EnableJpaAuditing
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {

//        return new AuditorAwareImpl();
        return SecurityUtils::getCurrentAuditor;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Simple auditor implementation
     * In production, this should get the current user from security context
     */
//    public static class AuditorAwareImpl implements AuditorAware<UUID> {
//        @Override
//        public Optional<UUID> getCurrentAuditor() {
//            // For now, return a default UUID
//            // In production, extract from JWT token or security context
//            return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
//        }
//    }
}
