package nl.metafactory.agentruntime.definition.demo;

import com.embabel.agent.api.tool.Tool;
import nl.metafactory.agentruntime.definition.tool.RegisteredAgentTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("agent-demo")
public class DemoAgentTools {

    @Bean
    RegisteredAgentTool demoCustomerRead() {
        return new RegisteredAgentTool("customer.read", Tool.create("customer_read",
                "Returns the account status of the calling customer (demo data, no real customer system).",
                input -> Tool.Result.text("{\"status\":\"A\",\"meaning\":\"active\",\"demo\":true}")));
    }
}
