package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.LlmOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "spring.config.location=file:src/main/resources/",
        "AI_PROVIDER=OpenAI",
        "AI_MODEL=gpt-4o",
        "OPENAI_API_KEY=sk-test-only",
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions"
})
@ActiveProfiles("local")
class AiProviderOpenAiBootTest {

    @Autowired ApplicationContext context;

    @Test
    void onlyTheConfiguredOpenAiModelIsRegisteredAndNoGatewayModel() {
        var llms = Arrays.stream(context.getBeanNamesForType(SpringAiLlmService.class))
                .map(name -> context.getBean(name, SpringAiLlmService.class))
                .toList();

        assertThat(llms).singleElement().satisfies(llm -> {
            assertThat(llm.getName()).isEqualTo("gpt-4o");
            assertThat(llm.getProvider()).isEqualTo("OpenAI");
            var options = (OpenAiChatOptions) llm.getOptionsConverter().convertOptions(LlmOptions.withDefaults(), "gpt-4o");
            assertThat(options.getModel()).isEqualTo("gpt-4o");
        });
        assertThat(context.getBeanNamesForType(LiteLlmGatewayModelConfig.class)).isEmpty();
        assertThat(context.getEnvironment().getProperty("embabel.models.llms.agent-chat")).isEqualTo("gpt-4o");
        assertThat(context.getEnvironment().getProperty("embabel.models.default-embedding-model"))
                .isEqualTo("text-embedding-3-small");
    }
}
