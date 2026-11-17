package nl.metafactory.agentruntime.config;

import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.SpringAiEmbeddingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class DirectModelConfigTest {

    private final DirectModelConfig config = new DirectModelConfig();

    @Test
    void anthropicOptionsCarryTheModelTimeoutAndSamplingWithoutTopPNextToATemperature() {
        var options = LlmOptions.withDefaults().withTemperature(0.5).withTopP(0.9).withTopK(40).withMaxTokens(256);

        var converted = (AnthropicChatOptions) DirectModelConfig.convertToAnthropicOptions(600L, options, "claude-sonnet-4-5");

        assertThat(converted.getModel()).isEqualTo("claude-sonnet-4-5");
        assertThat(converted.getMaxTokens()).isEqualTo(256);
        assertThat(converted.getTemperature()).isEqualTo(0.5);
        assertThat(converted.getTopP()).isNull();
        assertThat(converted.getTopK()).isEqualTo(40);
        assertThat(converted.getTimeout()).isEqualTo(Duration.ofSeconds(600));
    }

    @Test
    void anthropicOptionsWithoutATemperatureKeepTopPAndTheDefaultMaxTokens() {
        var converted = (AnthropicChatOptions) DirectModelConfig.convertToAnthropicOptions(
                30L, LlmOptions.withDefaults().withTopP(0.9), "claude-haiku-4-5");

        assertThat(converted.getTopP()).isEqualTo(0.9);
        assertThat(converted.getMaxTokens()).isEqualTo(AnthropicChatOptions.DEFAULT_MAX_TOKENS);
    }

    @Test
    void otherLogsNotConfiguredAtStartupAndOnEveryCall(CapturedOutput output) {
        var llm = config.notConfiguredLlm();

        assertThat(llm.getName()).isEqualTo(AiProviderEnvironmentPostProcessor.NOT_CONFIGURED);
        assertThat(llm.getProvider()).isEqualTo("Other");
        assertThat(output).contains("Not configured");
        assertThatThrownBy(() -> llm.getModel().call(new Prompt("hello")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(NotConfiguredChatModel.MESSAGE);
        assertThat(output.getOut().split("Not configured", -1)).hasSize(3);
    }

    @Test
    void withoutAnEmbeddingProviderEmbeddingsFailExplicitly() {
        var embeddings = (SpringAiEmbeddingService) config.noEmbeddingService();

        assertThat(embeddings.getName()).isEqualTo(AiProviderEnvironmentPostProcessor.NO_EMBEDDING);
        assertThatThrownBy(() -> embeddings.embed("text"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(DirectModelConfig.NO_EMBEDDING_MESSAGE);
    }
}
