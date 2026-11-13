package nl.metafactory.agentruntime.definition.skill;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SkillPackage(
        String id,
        String description,
        String instructions,
        Map<String, String> resources,
        List<String> scriptPaths,
        List<String> declaredAllowedTools,
        String contentHash
) {

    public SkillPackage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(instructions, "instructions");
        Objects.requireNonNull(contentHash, "contentHash");
        resources = Map.copyOf(resources == null ? Map.of() : resources);
        scriptPaths = List.copyOf(scriptPaths == null ? List.of() : scriptPaths);
        declaredAllowedTools = List.copyOf(declaredAllowedTools == null ? List.of() : declaredAllowedTools);
    }

    @Override
    public String toString() {
        return "SkillPackage{id=" + id + ", resources=" + resources.keySet() + ", contentHash=" + contentHash + "}";
    }
}
