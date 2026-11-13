package nl.metafactory.agentruntime.definition.source;

import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.agentruntime.definition.model.ProviderResult;
import nl.metafactory.common.agentdefinition.AgentMarkdownParser;
import nl.metafactory.common.agentdefinition.InvalidDefinitionException;
import nl.metafactory.common.agentdefinition.SourceStatus;
import nl.metafactory.common.agentruntime.AgentExecutionRequest.DefinitionSnapshot;

public class SnapshotAgentDefinitionProvider implements AgentDefinitionProvider {

    private final DefinitionSnapshot snapshot;
    private final AgentMarkdownParser parser;

    public SnapshotAgentDefinitionProvider(DefinitionSnapshot snapshot, AgentMarkdownParser parser) {
        this.snapshot = snapshot;
        this.parser = parser;
    }

    @Override
    public DefinitionSource source() {
        return DefinitionSource.LOCAL;
    }

    @Override
    public ProviderResult lookup(String agentId) {
        if (snapshot.status() != SourceStatus.FOUND) {
            return ProviderResult.of(snapshot.status(), snapshot.reason());
        }
        try {
            var doc = parser.parse(snapshot.markdown());
            return doc.id().equals(agentId) ? ProviderResult.found(doc)
                    : ProviderResult.of(SourceStatus.INVALID, "frontmatter id does not match the requested agent");
        } catch (InvalidDefinitionException e) {
            return ProviderResult.of(SourceStatus.INVALID, e.getMessage());
        }
    }
}
