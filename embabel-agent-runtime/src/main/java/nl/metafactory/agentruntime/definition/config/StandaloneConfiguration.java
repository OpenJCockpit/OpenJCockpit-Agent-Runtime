package nl.metafactory.agentruntime.definition.config;

import nl.metafactory.agentruntime.definition.binding.AgentTenantBindings;
import nl.metafactory.agentruntime.definition.binding.ConfiguredAgentTenantBindings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(StandaloneConfiguration.PROFILE)
@ConditionalOnProperty(name = "openjcockpit.agent-definitions.enabled", havingValue = "true")
@EnableConfigurationProperties(StandaloneProperties.class)
public class StandaloneConfiguration {

    public static final String PROFILE = "standalone";

    @Bean
    AgentTenantBindings agentTenantBindings(StandaloneProperties properties) {
        return new ConfiguredAgentTenantBindings(properties.tenantBindings());
    }
}
