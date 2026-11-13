package nl.metafactory.agentruntime.definition.resolver;

import nl.metafactory.common.agentruntime.AgentTenantBinding;
import nl.metafactory.agentruntime.definition.config.AgentDefinitionProperties;
import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.common.agentdefinition.SourceStatus;

public class ConfiguredFallbackPolicy implements FallbackPolicy {

    private final AgentDefinitionProperties.Fallback config;

    public ConfiguredFallbackPolicy(AgentDefinitionProperties.Fallback config) {
        this.config = config;
    }

    @Override
    public boolean mayContinueAfter(AgentTenantBinding binding, DefinitionSource failedSource, SourceStatus status) {
        if (!binding.fallbackAllowed()) {
            return false;
        }
        if (failedSource == DefinitionSource.LAST_KNOWN_GOOD
                && (status == SourceStatus.NOT_FOUND || status == SourceStatus.UNAVAILABLE)) {
            return true;
        }
        return switch (status) {
            case UNAVAILABLE -> config.isOnUnavailable();
            case NOT_FOUND -> config.isOnNotFound() && binding.fallbackOnNotFound();
            default -> false;
        };
    }
}
