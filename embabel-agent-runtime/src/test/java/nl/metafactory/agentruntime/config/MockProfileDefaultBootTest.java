package nl.metafactory.agentruntime.config;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import nl.metafactory.agentruntime.drafts.DefinitionDraftService;
import nl.metafactory.common.agentruntime.DefinitionDrafts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = "spring.config.location=file:src/main/resources/")
class MockProfileDefaultBootTest {

    @Autowired ApplicationContext context;
    @Autowired Ai ai;
    @Autowired DefinitionDraftService drafts;

    @Test
    void withoutAnExplicitProfileTheMockProfileIsTheDefault() {
        var environment = context.getEnvironment();
        assertThat(environment.getActiveProfiles()).isEmpty();
        assertThat(environment.getDefaultProfiles()).containsExactly(MockModelConfig.PROFILE);
    }

    @Test
    void onlyTheMockModelsAreRegisteredAndNoProviderClientExists() {
        var llmNames = Arrays.stream(context.getBeanNamesForType(SpringAiLlmService.class))
                .map(name -> context.getBean(name, SpringAiLlmService.class).getName())
                .toList();
        assertThat(llmNames).containsExactly(MockModelConfig.LLM_NAME);
        assertThat(context.getBeanNamesForType(ChatModel.class)).isEmpty();
        assertThat(context.getBeanNamesForType(EmbeddingModel.class)).isEmpty();
        assertThat(context.getBeanNamesForType(OpenAiModelConfig.class)).isEmpty();
        assertThat(context.getBeanNamesForType(LiteLlmGatewayModelConfig.class)).isEmpty();
        assertThat(context.getBeanNamesForType(OllamaModelConfig.class)).isEmpty();
    }

    @Test
    void theDefaultLlmIsTheMock() {
        assertThat(ai.withDefaultLlm().generateText("hello")).isEqualTo("[mock] Mock response to: hello");
    }

    @ParameterizedTest
    @ValueSource(strings = {"coding", "best", "cheapest", "agent-chat", "customer-chat"})
    void everyConfiguredRoleResolvesToTheMock(String role) {
        assertThat(ai.withLlmByRole(role).generateText("hello")).isEqualTo("[mock] Mock response to: hello");
    }

    @Test
    void definitionDraftsGetStructuredOutputThroughTheDefaultLlm() {
        var agent = (DefinitionDrafts.AgentDraft) drafts.draft(DefinitionDrafts.Kind.AGENT, "release notes writer");
        var subagent = (DefinitionDrafts.SubagentDraft) drafts.draft(DefinitionDrafts.Kind.SUBAGENT, "changelog reader");
        var skill = (DefinitionDrafts.SkillDraft) drafts.draft(DefinitionDrafts.Kind.SKILL, "summarise commits");

        assertThat(agent.name()).isEqualTo("mock-name");
        assertThat(agent.subagentNames()).containsExactly("mock-subagentNames");
        assertThat(subagent.parentAgent()).isEqualTo("mock-parentAgent");
        assertThat(skill.inputContract()).isEqualTo("mock-inputContract");
        assertThat(drafts.draft(DefinitionDrafts.Kind.AGENT, "release notes writer")).isEqualTo(agent);
    }

    @Test
    void embeddingsFailExplicitly() {
        var embeddings = ai.withDefaultEmbeddingService();
        assertThat(embeddings.getName()).isEqualTo(MockModelConfig.EMBEDDING_NAME);
        assertThatThrownBy(() -> embeddings.embed("text"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(UnsupportedEmbeddingModel.MESSAGE);
    }
}
