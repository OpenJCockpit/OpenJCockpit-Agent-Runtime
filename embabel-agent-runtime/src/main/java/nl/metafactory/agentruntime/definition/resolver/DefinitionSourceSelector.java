package nl.metafactory.agentruntime.definition.resolver;

import nl.metafactory.common.agentruntime.AgentTenantBinding;

@FunctionalInterface
public interface DefinitionSourceSelector {

    DefinitionSelection select(AgentTenantBinding binding);
}
