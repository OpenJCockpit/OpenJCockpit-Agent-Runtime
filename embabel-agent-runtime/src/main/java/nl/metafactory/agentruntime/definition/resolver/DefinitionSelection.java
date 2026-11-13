package nl.metafactory.agentruntime.definition.resolver;

import nl.metafactory.common.agentdefinition.AgentDefinitionDocument;
import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.agentruntime.definition.model.SourceAttempt;

import java.util.List;

public record DefinitionSelection(AgentDefinitionDocument document, DefinitionSource source, List<SourceAttempt> attempts) {

    public DefinitionSelection {
        attempts = List.copyOf(attempts);
    }
}
