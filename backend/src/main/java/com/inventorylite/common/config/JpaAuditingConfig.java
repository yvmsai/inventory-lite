package com.inventorylite.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "currentUserAuditor")
class JpaAuditingConfig {

    /** The auditor is the user id in the JWT subject; empty for system writes (seeding, bootstrap). */
    @Bean
    AuditorAware<Long> currentUserAuditor() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                return Optional.of(Long.valueOf(jwt.getSubject()));
            }
            return Optional.empty();
        };
    }
}
