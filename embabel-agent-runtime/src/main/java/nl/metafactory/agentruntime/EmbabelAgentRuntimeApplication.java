package nl.metafactory.agentruntime;

import com.embabel.agent.config.annotation.EnableAgents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAgents
public class EmbabelAgentRuntimeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmbabelAgentRuntimeApplication.class, args);
    }
}
