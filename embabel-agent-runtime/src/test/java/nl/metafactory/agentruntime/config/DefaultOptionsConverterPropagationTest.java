package nl.metafactory.agentruntime.config;

import com.embabel.common.ai.model.DefaultOptionsConverter;
import com.embabel.common.ai.model.LlmOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.ChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultOptionsConverterPropagationTest {

    @Test
    void propagatesTemperatureTopPMaxTokensPresencePenaltyAndFrequencyPenalty() {
        LlmOptions options = LlmOptions.withDefaults()
                .withTemperature(0.42)
                .withTopP(0.77)
                .withMaxTokens(1234)
                .withPresencePenalty(0.11)
                .withFrequencyPenalty(0.22);

        ChatOptions converted = DefaultOptionsConverter.INSTANCE.convertOptions(options, "gpt-4.1-mini");

        assertThat(converted.getTemperature()).as("temperature must propagate").isEqualTo(0.42);
        assertThat(converted.getTopP()).as("topP must propagate").isEqualTo(0.77);
        assertThat(converted.getMaxTokens()).as("maxTokens must propagate").isEqualTo(1234);
        assertThat(converted.getPresencePenalty()).as("presencePenalty must propagate").isEqualTo(0.11);
        assertThat(converted.getFrequencyPenalty()).as("frequencyPenalty must propagate").isEqualTo(0.22);
        assertThat(converted.getModel()).as("model must propagate").isEqualTo("gpt-4.1-mini");
    }

    @Test
    void doesNotPropagateTopKConfirmingNoRegressionSinceOpenAiNeverSupportedIt() {
        LlmOptions options = LlmOptions.withDefaults().withTopK(50);

        ChatOptions converted = DefaultOptionsConverter.INSTANCE.convertOptions(options, "gpt-4.1-mini");

        assertThat(converted.getTopK())
                .as("topK is measured here to be dropped by DefaultOptionsConverter; this matches "
                        + "the deleted Embabel 0.2.0 OpenAiChatOptionsConverter's behavior exactly, "
                        + "since OpenAI's Chat Completions API has no top_k parameter — not a regression")
                .isNull();
    }
}
