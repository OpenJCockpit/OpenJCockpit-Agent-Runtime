package nl.metafactory.agentruntime.definition.binding;

import nl.metafactory.common.agentruntime.AgentTenantBinding;

import java.util.Optional;

@FunctionalInterface
public interface AgentTenantBindings {

    Optional<AgentTenantBinding> find(String tenantId, String agentId);
}
