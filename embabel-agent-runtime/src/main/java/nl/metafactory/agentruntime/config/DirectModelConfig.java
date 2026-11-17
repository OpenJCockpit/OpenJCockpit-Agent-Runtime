package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.DefaultOptionsConverter;
import com.embabel.common.ai.model.EmbeddingService;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.OptionsConverter;
import com.embabel.common.ai.model.SpringAiEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@Profile("!" + MockModelConfig.PROFILE)
public class DirectModelConfig {

    static final String NO_EMBEDDING_MESSAGE = "AI_PROVIDER provides no embeddings; use OpenAI or LiteLLM for embeddings";

    private static final Logger log = LoggerFactory.getLogger(DirectModelConfig.class);

    @Bean
    @ConditionalOnAiProvider(AiProvider.OPENAI)
    SpringAiLlmService openAiLlm(OpenAiChatModel chatModel,
                                 @Value("${" + AiProviderEnvironmentPostProcessor.MODEL + "}") String model,
                                 @Value("${openjcockpit.llm.read-timeout-seconds:600}") long readTimeoutSeconds) {
        return llm(model, AiProvider.OPENAI, chatModel,
                (options, name) -> LiteLlmGatewayModelConfig.convertToGatewayOptions(readTimeoutSeconds, options, name));
    }

    @Bean
    @ConditionalOnAiProvider(AiProvider.ANTHROPIC)
    SpringAiLlmService anthropicLlm(AnthropicChatModel chatModel,
                                    @Value("${" + AiProviderEnvironmentPostProcessor.MODEL + "}") String model,
                                    @Value("${openjcockpit.llm.read-timeout-seconds:600}") long readTimeoutSeconds) {
        return llm(model, AiProvider.ANTHROPIC, chatModel,
                (options, name) -> convertToAnthropicOptions(readTimeoutSeconds, options, name));
    }

    @Bean
    @ConditionalOnAiProvider(AiProvider.OTHER)
    SpringAiLlmService notConfiguredLlm() {
        log.warn(NotConfiguredChatModel.MESSAGE);
        return new SpringAiLlmService(AiProviderEnvironmentPostProcessor.NOT_CONFIGURED, AiProvider.OTHER.label(),
                new NotConfiguredChatModel(), DefaultOptionsConverter.INSTANCE);
    }

    @Bean
    @ConditionalOnAiProvider({AiProvider.ANTHROPIC, AiProvider.OTHER})
    EmbeddingService noEmbeddingService() {
        return new SpringAiEmbeddingService(AiProviderEnvironmentPostProcessor.NO_EMBEDDING, "None",
                new UnsupportedEmbeddingModel(NO_EMBEDDING_MESSAGE), null, null);
    }

    private static SpringAiLlmService llm(String model, AiProvider provider, ChatModel chatModel,
                                          OptionsConverter optionsConverter) {
        log.info("LLM configured directly at {}: model={}", provider.label(), model);
        return new SpringAiLlmService(model, provider.label(), chatModel, optionsConverter);
    }

    static ChatOptions convertToAnthropicOptions(long readTimeoutSeconds, LlmOptions options, String model) {
        Double temperature = options.getTemperature();
        return AnthropicChatOptions.builder()
                .model(model)
                .maxTokens(options.getMaxTokens())
                .temperature(temperature)
                .topP(temperature == null ? options.getTopP() : null)
                .topK(options.getTopK())
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .build();
    }
}
