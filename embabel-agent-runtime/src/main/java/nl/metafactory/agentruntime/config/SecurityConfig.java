package nl.metafactory.agentruntime.config;

import nl.metafactory.common.security.ResourceServerSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Default (and {@code prod}): every API call needs a Keycloak-issued JWT. {@link DevSecurityConfig} replaces it. */
@Configuration
@EnableWebSecurity
@Profile("!" + DevSecurityConfig.PROFILE)
@Import(ResourceServerSecurityConfig.class)
public class SecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return ResourceServerSecurityConfig.buildApiChain(http, new JwtAuthenticationConverter(), auth -> auth
                .requestMatchers("/actuator/health/readiness", "/actuator/health/liveness").permitAll()
                .anyRequest().authenticated());
    }
}
