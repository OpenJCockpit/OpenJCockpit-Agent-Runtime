package nl.metafactory.common.agentdefinition;

import java.util.List;
import java.util.Objects;

public record AgentDefinitionDocument(
        String id,
        String name,
        String version,
        String description,
        String model,
        List<String> skills,
        List<String> optionalSkills,
        List<String> tools,
        List<String> guardrails,
        String instructions,
        String contentHash,
        String rawMarkdown
) {

    public AgentDefinitionDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(instructions, "instructions");
        Objects.requireNonNull(contentHash, "contentHash");
        skills = List.copyOf(skills == null ? List.of() : skills);
        optionalSkills = List.copyOf(optionalSkills == null ? List.of() : optionalSkills);
        tools = List.copyOf(tools == null ? List.of() : tools);
        guardrails = List.copyOf(guardrails == null ? List.of() : guardrails);
    }

    @Override
    public String toString() {
        return "AgentDefinitionDocument{id=" + id + ", version=" + version + ", contentHash=" + contentHash + "}";
    }
}
