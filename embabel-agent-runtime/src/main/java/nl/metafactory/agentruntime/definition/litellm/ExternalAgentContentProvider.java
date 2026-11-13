package nl.metafactory.agentruntime.definition.litellm;

import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.agentruntime.definition.model.ProviderResult;
import nl.metafactory.common.agentdefinition.SourceStatus;
import nl.metafactory.agentruntime.definition.source.AgentDefinitionProvider;

public class ExternalAgentContentProvider implements AgentDefinitionProvider {

    public static final String REASON =
            "LiteLLM v1.101.2 exposes no agent-definition content API (Agent Card metadata only)";

    @Override
    public DefinitionSource source() {
        return DefinitionSource.REMOTE_LITELLM;
    }

    @Override
    public ProviderResult lookup(String agentId) {
        return ProviderResult.of(SourceStatus.UNSUPPORTED, REASON);
    }
}
