package nl.metafactory.common.agentruntime;

import java.util.Objects;
import java.util.Set;

public record AgentTenantBinding(
        String tenantId,
        String agentId,
        boolean enabled,
        String modelAlias,
        String pinnedVersion,
        boolean fallbackAllowed,
        boolean fallbackOnNotFound,
        Set<String> allowedTools,
        Set<String> requiredGuardrails
) {

    public AgentTenantBinding {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(agentId, "agentId");
        allowedTools = Set.copyOf(allowedTools == null ? Set.of() : allowedTools);
        requiredGuardrails = Set.copyOf(requiredGuardrails == null ? Set.of() : requiredGuardrails);
    }
}
