package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.ModelProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OllamaAutoConfigurationAbsenceBootTest {

    @MockitoBean ModelProvider modelProvider;

    @Autowired ApplicationContext context;

    @Test
    void noOllamaAutoConfiguredBeansExistInTheContext() {
        assertThat(context.getBeanNamesForType(OllamaChatModel.class))
                .as("no auto-configured OllamaChatModel bean must exist - ADR-005 pins spring.ai.model.chat to openai")
                .isEmpty();
        assertThat(context.getBeanNamesForType(OllamaEmbeddingModel.class))
                .as("no auto-configured OllamaEmbeddingModel bean must exist - ADR-005 pins spring.ai.model.embedding to openai")
                .isEmpty();
        assertThat(context.getBeanNamesForType(OllamaApi.class))
                .as("Spring AI 2.0.1 disclosed deviation: OllamaApiAutoConfiguration is always-on (classpath-gated only); expect exactly the harmless 'ollamaApi' bean and nothing else")
                .containsExactly("ollamaApi");
    }

    @Test
    void chatModelAndEmbeddingModelBeanCountsMatchThePreOllamaBaseline() {
        assertThat(context.getBeanNamesForType(ChatModel.class))
                .as("WP-0 baseline: exactly one ChatModel bean (openAiChatModel) must remain - adding the "
                        + "Ollama starter must not register a second, competing ChatModel bean")
                .containsExactly("openAiChatModel");
        assertThat(context.getBeanNamesForType(EmbeddingModel.class))
                .as("WP-0 baseline: exactly one EmbeddingModel bean (openAiEmbeddingModel) must remain")
                .containsExactly("openAiEmbeddingModel");
    }

    @Test
    void bothHandBuiltLlmBeansArePresentAndNoOthers() {
        assertThat(Set.of(context.getBeanNamesForType(SpringAiLlmService.class)))
                .as("exactly the two hand-built Llm beans must be registered, by name")
                .isEqualTo(Set.of("litellmCodingLlm", "gpt41Mini"));
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
            properties = "openjcockpit.llm.coding.route=direct")
    class DirectRouteRevertB {

        @Test
        void directRouteActivatesTheOllamaNativeBeanSetInsteadOfTheGatewayOne() {
            assertThat(Set.of(context.getBeanNamesForType(SpringAiLlmService.class)))
                    .as("setting openjcockpit.llm.coding.route=direct must activate the pre-gateway bean set")
                    .isEqualTo(Set.of("ollamaCodingLlm", "gpt41Mini"));
            assertThat(Set.of(context.getBeanNamesForType(ChatModel.class)))
                    .as("the direct route must not expose any extra ChatModel bean")
                    .isEqualTo(Set.of("openAiChatModel"));
        }
    }
}
