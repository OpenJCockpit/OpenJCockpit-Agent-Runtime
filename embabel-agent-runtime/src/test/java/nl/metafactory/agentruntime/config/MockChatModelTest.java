package nl.metafactory.agentruntime.config;

import com.embabel.common.ai.converters.JacksonOutputConverter;
import com.embabel.common.ai.converters.RequiredFieldNormalization;
import nl.metafactory.common.agentruntime.DefinitionDrafts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockChatModelTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final MockChatModel model = new MockChatModel("mock", json, new JsonSchemaInstanceGenerator());

    private String answer(Prompt prompt) {
        return model.call(prompt).getResult().getOutput().getText();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Write requirements for a password reset.", "Summarise the release", ""})
    void textRequestsGetADeterministicAnswerNamingTheModelAndTheUserMessage(String message) {
        var prompt = new Prompt(List.of(new SystemMessage("You are a business analyst."), new UserMessage(message)));

        assertThat(answer(prompt)).isEqualTo("[mock] Mock response to: " + message);
        assertThat(answer(prompt)).isEqualTo(answer(prompt));
    }

    @ParameterizedTest
    @ValueSource(classes = {DefinitionDrafts.AgentDraft.class, DefinitionDrafts.SubagentDraft.class,
            DefinitionDrafts.SkillDraft.class})
    <T> void structuredRequestsAnswerJsonThatEmbabelsConverterTurnsIntoTheRequestedType(Class<T> type) {
        var converter = new JacksonOutputConverter<>(type, json, RequiredFieldNormalization.ENABLED);
        var prompt = new Prompt(new UserMessage("Draft a release notes writer." + converter.getFormat()));

        T result = converter.convert(answer(prompt));

        assertThat(result).isInstanceOf(type).hasNoNullFieldsOrProperties();
        assertThat(answer(prompt)).isEqualTo(answer(prompt));
    }

    @Test
    void structuredAnswersFillEveryFieldOfAnAgentDraft() {
        var converter = new JacksonOutputConverter<>(DefinitionDrafts.AgentDraft.class, json,
                RequiredFieldNormalization.ENABLED);

        var draft = converter.convert(answer(new Prompt(new UserMessage("concept" + converter.getFormat()))));

        assertThat(draft.name()).isEqualTo("mock-name");
        assertThat(draft.skillNames()).containsExactly("mock-skillNames");
        assertThat(draft.mcpTools()).containsExactly(new DefinitionDrafts.ToolRef("mock-name", "mock-description"));
        assertThat(draft.builtIn()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "``` {\"type\":\"string\"}"})
    void aStructuredRequestWithoutAFencedSchemaFailsExplicitly(String tail) {
        var prompt = new Prompt(new UserMessage("concept\n" + MockChatModel.SCHEMA_MARKER + tail));

        assertThatThrownBy(() -> model.call(prompt))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("no fenced JSON schema");
    }

    @Test
    void streamingIsNotSupported() {
        assertThatThrownBy(() -> model.stream(new Prompt("hello")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
