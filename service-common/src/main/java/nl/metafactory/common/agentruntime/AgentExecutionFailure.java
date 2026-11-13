package nl.metafactory.common.agentruntime;

public record AgentExecutionFailure(String executionId, String reason, String message) {

    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    public static final String FORBIDDEN = "FORBIDDEN";

    public static final String DUPLICATE_EXECUTION = "DUPLICATE_EXECUTION";

    public static final String GUARDRAIL_VIOLATION = "GUARDRAIL_VIOLATION";

    public static final String EXECUTION_FAILED = "EXECUTION_FAILED";

    public static final String UNKNOWN_EXECUTION = "UNKNOWN_EXECUTION";
}
