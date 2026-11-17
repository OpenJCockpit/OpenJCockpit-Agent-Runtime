package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import org.junit.jupiter.api.Test;
import com.embabel.common.ai.model.LlmOptions;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "spring.config.location=file:src/main/resources/",
        "AI_PROVIDER=Anthropic",
        "AI_MODEL=claude-sonnet-4-5",
        "spring.ai.anthropic.api-key=sk-ant-test-only",
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions"
})
@ActiveProfiles("local")
class AiProviderAnthropicBootTest {

    @Autowired ApplicationContext context;

    @Test
    void theAnthropicModelServesEveryRoleWithoutAnOpenAiKey() {
        var llms = Arrays.stream(context.getBeanNamesForType(SpringAiLlmService.class))
                .map(name -> context.getBean(name, SpringAiLlmService.class))
                .toList();

        assertThat(llms).singleElement().satisfies(llm -> {
            assertThat(llm.getName()).isEqualTo("claude-sonnet-4-5");
            assertThat(llm.getProvider()).isEqualTo("Anthropic");
            assertThat(llm.getModel()).isInstanceOf(AnthropicChatModel.class);
            var options = (AnthropicChatOptions) llm.getOptionsConverter()
                    .convertOptions(LlmOptions.withDefaults(), "claude-sonnet-4-5");
            assertThat(options.getModel()).isEqualTo("claude-sonnet-4-5");
            assertThat(options.getTimeout()).isEqualTo(Duration.ofSeconds(600));
        });
        assertThat(context.getBeanNamesForType(ChatModel.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(EmbeddingModel.class)).isEmpty();
        assertThat(context.getEnvironment().getProperty("embabel.models.llms.customer-chat")).isEqualTo("claude-sonnet-4-5");
    }
}
