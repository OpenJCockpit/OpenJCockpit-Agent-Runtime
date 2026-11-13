package nl.metafactory.agentruntime.definition.resolver;

import nl.metafactory.common.agentruntime.AgentTenantBinding;
import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.common.agentdefinition.SourceStatus;

@FunctionalInterface
public interface FallbackPolicy {

    FallbackPolicy NONE = (binding, source, status) -> false;

    boolean mayContinueAfter(AgentTenantBinding binding, DefinitionSource failedSource, SourceStatus status);
}
