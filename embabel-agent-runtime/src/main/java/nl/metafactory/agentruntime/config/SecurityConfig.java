package nl.metafactory.agentruntime.config;

import nl.metafactory.common.security.ResourceServerSecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@code ENABLE_KEYCLOAK=true} (default, always in {@code prod}): every API call needs a Keycloak-issued JWT.
 * {@link KeycloakDisabledSecurityConfig} replaces it.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnBooleanProperty(name = SecurityConfig.KEYCLOAK_ENABLED, matchIfMissing = true)
@Import(ResourceServerSecurityConfig.class)
public class SecurityConfig {

    public static final String KEYCLOAK_ENABLED = "openjcockpit.security.keycloak.enabled";

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return ResourceServerSecurityConfig.buildApiChain(http, new JwtAuthenticationConverter(), auth -> auth
                .requestMatchers("/actuator/health/readiness", "/actuator/health/liveness").permitAll()
                .anyRequest().authenticated());
    }
}
