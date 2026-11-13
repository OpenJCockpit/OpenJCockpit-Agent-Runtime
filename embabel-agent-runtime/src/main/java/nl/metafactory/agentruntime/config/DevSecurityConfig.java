package nl.metafactory.agentruntime.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import nl.metafactory.common.security.ProductionConfigGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Profile {@code dev}: runs without Keycloak. No token is validated; every request is authenticated as one fixed
 * local identity carrying the tenant and trusted-caller claims the controllers read. Never combine with
 * {@code prod} ({@link ProductionConfigGuard} rejects it).
 */
@Configuration
@EnableWebSecurity
@Profile(DevSecurityConfig.PROFILE)
@Import(ProductionConfigGuard.class)
public class DevSecurityConfig {

    public static final String PROFILE = "dev";

    private static final Logger log = LoggerFactory.getLogger(DevSecurityConfig.class);

    @Bean
    SecurityFilterChain devSecurityFilterChain(HttpSecurity http, Jwt devIdentity) throws Exception {
        log.warn("security.dev-mode: authentication is disabled, every request runs as '{}' with claims {}",
                devIdentity.getSubject(), devIdentity.getClaims());
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new DevIdentityFilter(devIdentity), AnonymousAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    Jwt devIdentity(
            @Value("${openjcockpit.dev.identity.username:developer}") String username,
            @Value("${openjcockpit.dev.identity.tenant:local}") String tenant,
            @Value("${openjcockpit.agent-definitions.tenant-claim:tenant_id}") String tenantClaim,
            RuntimeApiProperties runtimeApi
    ) {
        return Jwt.withTokenValue("dev")
                .header("alg", "none")
                .subject(username)
                .claim("preferred_username", username)
                .claim(tenantClaim, tenant)
                // the dev caller is also the trusted management plane, so /internal/v1/** is reachable locally
                .claim("azp", runtimeApi.getTrustedClientId())
                .issuedAt(Instant.EPOCH)
                .build();
    }

    static final class DevIdentityFilter extends OncePerRequestFilter {

        private final Jwt identity;

        DevIdentityFilter(Jwt identity) {
            this.identity = identity;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(identity, List.of()));
            chain.doFilter(request, response);
        }
    }
}
