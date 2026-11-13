package nl.metafactory.common.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Import(ProductionConfigGuard.class)
public class ResourceServerSecurityConfig {

    static final String[] PUBLIC_ACTUATOR = {
            "/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness", "/actuator/info"};

    @Bean
    @Order(1)
    SecurityFilterChain protectedResourceMetadataSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/.well-known/oauth-protected-resource/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(handling -> handling.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return OpenJCockpitRoles.hierarchy();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${openjcockpit.security.jwt-issuer}") String issuerUri
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    static RequestMatcher metricsScrapeMatcher(Environment environment) {
        Integer port = environment.getProperty("management.server.port", Integer.class);
        Integer serverPort = environment.getProperty("server.port", Integer.class);
        if (port == null || port <= 0 || port.equals(serverPort)) {
            return null;
        }
        return request -> request.getLocalPort() == port && "/actuator/prometheus".equals(request.getRequestURI());
    }

    public static SecurityFilterChain buildApiChain(HttpSecurity http) throws Exception {
        return buildApiChain(http, new JwtAuthenticationConverter());
    }

    public static SecurityFilterChain buildApiChain(HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter) throws Exception {
        return buildApiChain(http, jwtConverter, auth -> auth.anyRequest().authenticated());
    }

    public static SecurityFilterChain buildApiChain(HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter,
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> rules)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    auth.dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll();
                    auth.requestMatchers(PUBLIC_ACTUATOR).permitAll();
                    RequestMatcher scrape = metricsScrapeMatcher(
                            http.getSharedObject(ApplicationContext.class).getEnvironment());
                    if (scrape != null) {
                        auth.requestMatchers(scrape).permitAll();
                    }
                    rules.customize(auth);
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .accessDeniedHandler(new JsonAccessDeniedHandler())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
