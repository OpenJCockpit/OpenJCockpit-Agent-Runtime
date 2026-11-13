package nl.metafactory.agentruntime.definition.config;

import nl.metafactory.common.agentruntime.AgentTenantBinding;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("openjcockpit.standalone")
public record StandaloneProperties(List<AgentTenantBinding> tenantBindings) {

    public StandaloneProperties {
        tenantBindings = tenantBindings == null ? List.of() : List.copyOf(tenantBindings);
    }
}
