package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "spring.config.location=file:src/main/resources/",
        "OPENAI_API_KEY=sk-test-only",
        "OLLAMA_MODEL=qwen-isolation-test",
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions"
})
@ActiveProfiles("standalone")
class RealProviderProfileIsolationTest {

    @Autowired ApplicationContext context;

    @Test
    void anExplicitProfileUsesTheRealProvidersAndNeverTheMock() {
        assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("standalone");
        assertThat(context.getBeanNamesForType(MockModelConfig.class)).isEmpty();
        var llmNames = Arrays.stream(context.getBeanNamesForType(SpringAiLlmService.class))
                .map(name -> context.getBean(name, SpringAiLlmService.class).getName())
                .toList();
        assertThat(llmNames).contains("gpt-4.1-mini", "qwen-isolation-test").doesNotContain(MockModelConfig.LLM_NAME);
        assertThat(context.getEnvironment().getProperty("embabel.models.default-llm")).isEqualTo("qwen-isolation-test");
    }
}
