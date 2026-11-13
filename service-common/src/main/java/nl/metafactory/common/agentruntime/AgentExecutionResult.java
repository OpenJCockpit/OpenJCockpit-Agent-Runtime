package nl.metafactory.common.agentruntime;

public record AgentExecutionResult(String executionId, String agentId, String version, String contentHash,
                                   String traceId, String text) {

    @Override
    public String toString() {
        return "AgentExecutionResult[executionId=" + executionId + ", agentId=" + agentId + ", version=" + version
                + ", traceId=" + traceId + "]";
    }
}
