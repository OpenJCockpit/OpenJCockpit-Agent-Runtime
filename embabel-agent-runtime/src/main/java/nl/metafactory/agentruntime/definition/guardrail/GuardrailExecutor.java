package nl.metafactory.agentruntime.definition.guardrail;

import com.embabel.agent.api.validation.guardrails.GuardRail;

import java.util.List;
import java.util.Set;

public interface GuardrailExecutor {

    String guardrailId();

    String executorName();

    Set<GuardrailScope> scopes();

    boolean operational();

    default boolean operationalFor(String modelAlias) {
        return operational();
    }

    default List<GuardRail> embabelGuardRails() {
        return List.of();
    }
}
