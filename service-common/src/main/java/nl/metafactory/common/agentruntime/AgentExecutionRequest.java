package nl.metafactory.common.agentruntime;

import nl.metafactory.common.agentdefinition.SourceStatus;

import java.util.Objects;
import java.util.regex.Pattern;

public record AgentExecutionRequest(String executionId, String tenantId, String agentId, String principal,
                                    String workflowRunId, String traceparent, String message,
                                    DefinitionSnapshot definition) {

    public static final Pattern EXECUTION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,160}");

    public record DefinitionSnapshot(SourceStatus status, String markdown, String reason) {

        public DefinitionSnapshot {
            Objects.requireNonNull(status, "status");
            if ((status == SourceStatus.FOUND) != (markdown != null)) {
                throw new IllegalArgumentException("markdown is present if and only if status is FOUND");
            }
        }

        public static DefinitionSnapshot found(String markdown) {
            return new DefinitionSnapshot(SourceStatus.FOUND, markdown, null);
        }

        public static DefinitionSnapshot of(SourceStatus status, String reason) {
            return new DefinitionSnapshot(status, null, reason);
        }

        @Override
        public String toString() {
            return "DefinitionSnapshot[status=" + status + ", reason=" + reason + "]";
        }
    }

    @Override
    public String toString() {
        return "AgentExecutionRequest[executionId=" + executionId + ", tenantId=" + tenantId + ", agentId=" + agentId
                + ", workflowRunId=" + workflowRunId + ", definition=" + definition + "]";
    }
}
