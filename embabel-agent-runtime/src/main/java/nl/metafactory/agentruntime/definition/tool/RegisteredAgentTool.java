package nl.metafactory.agentruntime.definition.tool;

import com.embabel.agent.api.tool.Tool;
import nl.metafactory.common.agentdefinition.Identifiers;

import java.util.Objects;

public record RegisteredAgentTool(String id, Tool tool) {

    public RegisteredAgentTool {
        if (id == null || id.length() > Identifiers.MAX_LENGTH || !Identifiers.QUALIFIED.matcher(id).matches()) {
            throw new IllegalArgumentException("invalid tool id");
        }
        Objects.requireNonNull(tool, "tool");
        if (!tool.getDefinition().getName().equals(llmName(id))) {
            throw new IllegalArgumentException("tool " + id + " must be named " + llmName(id) + " for the LLM");
        }
    }

    public static String llmName(String id) {
        return id.replaceAll("[^a-z0-9_-]", "_");
    }
}
