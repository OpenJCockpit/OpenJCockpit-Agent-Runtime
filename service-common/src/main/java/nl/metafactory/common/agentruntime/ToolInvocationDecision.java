package nl.metafactory.common.agentruntime;

import java.util.Map;

public record ToolInvocationDecision(boolean allowed, String reason) {

    public record Request(String workflowId, String workflowRunId, String tenantId, String agentId, String toolId,
                          String principal, Map<String, String> attributes) {

        public Request {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }
}
