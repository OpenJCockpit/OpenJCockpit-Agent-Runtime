package nl.metafactory.agentruntime.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiProviderTest {

    @ParameterizedTest
    @CsvSource({"OpenAI,OPENAI", "anthropic,ANTHROPIC", "LITELLM,LITELLM", " Other ,OTHER"})
    void theProviderNameIsMatchedIgnoringCase(String value, AiProvider expected) {
        assertThat(AiProvider.of(value)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void withoutAProviderLiteLlmIsUsed(String value) {
        assertThat(AiProvider.of(value)).isEqualTo(AiProvider.LITELLM);
    }

    @Test
    void anUnknownProviderNamesTheAllowedOnes() {
        assertThatThrownBy(() -> AiProvider.of("Gemini"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI_PROVIDER must be one of OpenAI, Anthropic, LiteLLM, Other, not: Gemini");
    }

    @Test
    void theProviderIsReadFromTheEnvironment() {
        assertThat(AiProvider.of(new MockEnvironment().withProperty(AiProvider.PROPERTY, "Anthropic")))
                .isEqualTo(AiProvider.ANTHROPIC);
        assertThat(AiProvider.of(new MockEnvironment())).isEqualTo(AiProvider.LITELLM);
    }
}
