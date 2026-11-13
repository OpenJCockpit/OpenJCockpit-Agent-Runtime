package nl.metafactory.agentruntime.definition.source;

import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.agentruntime.definition.model.ProviderResult;

public interface AgentDefinitionProvider {

    DefinitionSource source();

    ProviderResult lookup(String agentId);
}
