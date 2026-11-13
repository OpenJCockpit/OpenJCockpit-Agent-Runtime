package nl.metafactory.common.agentruntime;

public final class AgentRuntimeApi {

    public static final String EXECUTIONS = "/internal/v1/agent-executions";

    public static final String EXECUTION = EXECUTIONS + "/{executionId}";

    public static final String DEFINITION_DRAFT = "/internal/v1/definition-drafts/{kind}";

    public static final String MANAGEMENT = "/internal/agent-runtime/v1";

    public static final String TENANT_BINDING = MANAGEMENT + "/tenants/{tenantId}/agent-bindings/{agentId}";

    public static final String WORKFLOW_RUN = MANAGEMENT + "/workflow-runs/{runId}";

    public static final String TOOL_INVOCATION_DECISIONS = MANAGEMENT + "/tool-invocation-decisions";

    public static final String WORKFLOW_RUN_HEADER = "x-openjcockpit-workflow-run";

    private AgentRuntimeApi() {
    }
}
