package nl.metafactory.agentruntime.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiProviderEnvironmentPostProcessorTest {

    private final AiProviderEnvironmentPostProcessor processor = new AiProviderEnvironmentPostProcessor();

    private MockEnvironment process(String provider, String model) {
        var environment = new MockEnvironment().withProperty(AiProvider.PROPERTY, provider);
        if (model != null) {
            environment.setProperty(AiProviderEnvironmentPostProcessor.MODEL, model);
        }
        environment.setActiveProfiles("local");
        processor.postProcessEnvironment(environment, new SpringApplication());
        return environment;
    }

    private static void assertEveryRoleRunsOn(MockEnvironment environment, String model) {
        assertThat(environment.getProperty("embabel.models.default-llm")).isEqualTo(model);
        for (String role : new String[]{"coding", "best", "cheapest", "agent-chat", "customer-chat"}) {
            assertThat(environment.getProperty("embabel.models.llms." + role)).isEqualTo(model);
        }
    }

    @Test
    void openAiRunsEveryRoleOnTheModelAndKeepsItsEmbeddings() {
        var environment = process("OpenAI", " gpt-4o ");

        assertEveryRoleRunsOn(environment, "gpt-4o");
        assertThat(environment.getProperty("spring.ai.openai.chat.model")).isEqualTo("gpt-4o");
        assertThat(environment.getProperty("spring.ai.model.chat")).isNull();
        assertThat(environment.getProperty("spring.ai.model.embedding")).isNull();
    }

    @Test
    void anthropicSwitchesSpringAiToAnthropicWithoutEmbeddings() {
        var environment = process("Anthropic", "claude-sonnet-4-5");

        assertEveryRoleRunsOn(environment, "claude-sonnet-4-5");
        assertThat(environment.getProperty("spring.ai.model.chat")).isEqualTo("anthropic");
        assertThat(environment.getProperty("spring.ai.anthropic.chat.model")).isEqualTo("claude-sonnet-4-5");
        assertNoProviderModelsBesideChat(environment);
    }

    @Test
    void otherRunsEveryRoleOnTheNotConfiguredModelAndNeedsNoModelName() {
        var environment = process("Other", null);

        assertEveryRoleRunsOn(environment, AiProviderEnvironmentPostProcessor.NOT_CONFIGURED);
        assertThat(environment.getProperty("spring.ai.model.chat")).isEqualTo("none");
        assertNoProviderModelsBesideChat(environment);
    }

    private static void assertNoProviderModelsBesideChat(MockEnvironment environment) {
        for (String kind : new String[]{"embedding", "image", "moderation", "audio.speech", "audio.transcription"}) {
            assertThat(environment.getProperty("spring.ai.model." + kind)).isEqualTo("none");
        }
        assertThat(environment.getProperty("embabel.models.default-embedding-model"))
                .isEqualTo(AiProviderEnvironmentPostProcessor.NO_EMBEDDING);
        assertThat(environment.getProperty("embabel.models.embedding-services.best"))
                .isEqualTo(AiProviderEnvironmentPostProcessor.NO_EMBEDDING);
        assertThat(environment.getProperty("embabel.models.embedding-services.cheapest"))
                .isEqualTo(AiProviderEnvironmentPostProcessor.NO_EMBEDDING);
    }

    @Test
    void liteLlmChangesNothing() {
        var environment = process("LiteLLM", null);

        assertThat(environment.getPropertySources().contains(AiProviderEnvironmentPostProcessor.SOURCE)).isFalse();
    }

    @Test
    void theMockProfileKeepsItsMockModels() {
        var environment = new MockEnvironment().withProperty(AiProvider.PROPERTY, "OpenAI");
        environment.setDefaultProfiles(MockModelConfig.PROFILE);

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getPropertySources().contains(AiProviderEnvironmentPostProcessor.SOURCE)).isFalse();
    }

    @Test
    void openAiAndAnthropicNeedAModel() {
        assertThatThrownBy(() -> process("OpenAI", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI_MODEL is required when AI_PROVIDER=OpenAI");
        assertThatThrownBy(() -> process("Anthropic", " "))
                .hasMessage("AI_MODEL is required when AI_PROVIDER=Anthropic");
    }

    @Test
    void environmentVariablesStillOverrideTheProviderDefaults() {
        var environment = new StandardEnvironment();
        var sources = environment.getPropertySources();
        sources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new org.springframework.core.env.SystemEnvironmentPropertySource(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        Map.of("EMBABEL_MODELS_DEFAULT_LLM", "gpt-4.1")));
        sources.addLast(new org.springframework.core.env.MapPropertySource("application", Map.of(
                AiProvider.PROPERTY, "OpenAI",
                AiProviderEnvironmentPostProcessor.MODEL, "gpt-4o",
                "embabel.models.llms.coding", "qwen")));
        environment.setActiveProfiles("local");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("embabel.models.default-llm")).isEqualTo("gpt-4.1");
        assertThat(environment.getProperty("embabel.models.llms.coding")).isEqualTo("gpt-4o");
    }
}
