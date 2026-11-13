package nl.metafactory.agentruntime.definition.tool;

import nl.metafactory.agentruntime.definition.model.AgentResolutionException;
import nl.metafactory.agentruntime.definition.model.ResolutionFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ToolAccessPolicy {

    private final ToolRegistry registry;

    public ToolAccessPolicy(ToolRegistry registry) {
        this.registry = registry;
    }

    public List<RegisteredAgentTool> authorize(String agentId, List<String> requested, Set<String> serverAllowlist) {
        var bound = new ArrayList<RegisteredAgentTool>();
        for (String id : requested) {
            var tool = registry.find(id).orElseThrow(() -> new AgentResolutionException(ResolutionFailure.TOOL_DENIED,
                    "agent " + agentId + " requests unregistered tool " + id));
            if (serverAllowlist == null || !serverAllowlist.contains(id)) {
                throw new AgentResolutionException(ResolutionFailure.TOOL_DENIED,
                        "agent " + agentId + " is not allowed to use tool " + id);
            }
            bound.add(tool);
        }
        return List.copyOf(bound);
    }
}
