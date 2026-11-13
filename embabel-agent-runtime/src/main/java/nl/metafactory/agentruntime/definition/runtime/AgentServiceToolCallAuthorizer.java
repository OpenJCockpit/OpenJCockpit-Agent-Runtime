package nl.metafactory.agentruntime.definition.runtime;

import nl.metafactory.agentruntime.management.AgentServiceClient;
import nl.metafactory.common.agentruntime.ToolInvocationDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentServiceToolCallAuthorizer implements ToolCallAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceToolCallAuthorizer.class);

    private final AgentServiceClient agentService;

    public AgentServiceToolCallAuthorizer(AgentServiceClient agentService) {
        this.agentService = agentService;
    }

    @Override
    public Decision authorize(RunContext run, String toolId) {
        var workflow = run.workflow();
        var request = new ToolInvocationDecision.Request(workflow == null ? null : workflow.workflowId(),
                workflow == null ? run.runId() : workflow.workflowRunId(), run.tenantId(), run.agentId(), toolId,
                run.principal(),
                Map.of("traceId", run.traceId(), "agentVersion", run.version(), "agentRunId", run.runId()));
        try {
            var decision = agentService.decideToolInvocation(request);
            return decision.allowed() ? Decision.allow() : Decision.deny(decision.reason());
        } catch (RuntimeException e) {
            log.warn("agent.tool.decision.unavailable tool={} {}", toolId, run.logFields());
            return Decision.deny("policy decision unavailable");
        }
    }
}
