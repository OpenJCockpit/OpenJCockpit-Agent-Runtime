package nl.metafactory.agentruntime.definition.tool;

import com.embabel.agent.api.tool.Tool;
import nl.metafactory.agentruntime.definition.runtime.RunContext;

public interface RunScopedTool extends Tool {

    Tool.Result call(String input, RunContext run);

    @Override
    default Tool.Result call(String input) {
        return Tool.Result.error("this tool is only available inside an agent run");
    }
}
