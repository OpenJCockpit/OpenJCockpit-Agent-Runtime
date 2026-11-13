package nl.metafactory.agentruntime.definition.runtime;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.spi.loop.ToolNotFoundAction;
import com.embabel.agent.spi.loop.ToolNotFoundException;
import com.embabel.agent.spi.loop.ToolNotFoundPolicy;

import java.util.List;

final class StrictToolNotFoundPolicy implements ToolNotFoundPolicy {

    @Override
    public ToolNotFoundAction handle(String requestedTool, List<? extends Tool> availableTools) {
        return new ToolNotFoundAction.Throw(new ToolNotFoundException(requestedTool,
                availableTools.stream().map(t -> t.getDefinition().getName()).toList()));
    }
}
