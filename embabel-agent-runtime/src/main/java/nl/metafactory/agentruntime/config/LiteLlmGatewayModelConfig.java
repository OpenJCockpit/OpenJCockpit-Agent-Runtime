package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.OptionsConverter;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "openjcockpit.llm.coding.route", havingValue = "gateway", matchIfMissing = true)
public class LiteLlmGatewayModelConfig {

    private static final Logger log = LoggerFactory.getLogger(LiteLlmGatewayModelConfig.class);

    @Bean
    SpringAiLlmService litellmCodingLlm(
            @Value("${openjcockpit.llm.gateway.base-url:http://localhost:4000/v1}") String baseUrl,
            @Value("${openjcockpit.llm.gateway.api-key:sk-openjcockpit-embabel-local}") String apiKey,
            @Value("${openjcockpit.llm.ollama.model:qwen3.6:27b}") String model,
            @Value("${openjcockpit.llm.connect-timeout-seconds:45}") long connectTimeoutSeconds,
            @Value("${openjcockpit.llm.read-timeout-seconds:600}") long readTimeoutSeconds,
            ObservationRegistry observationRegistry) {
        return gatewayLlm(baseUrl, apiKey, model, connectTimeoutSeconds, readTimeoutSeconds, observationRegistry);
    }

    static SpringAiLlmService gatewayLlm(String baseUrl, String apiKey, String model, long connectTimeoutSeconds,
                                         long readTimeoutSeconds, ObservationRegistry observationRegistry) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .maxRetries(0)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(chatOptions)
                .observationRegistry(observationRegistry)
                .httpClientBuilderCustomizer(builder -> builder.timeout(
                        com.openai.core.Timeout.builder()
                                .connect(Duration.ofSeconds(connectTimeoutSeconds))
                                .read(Duration.ofSeconds(readTimeoutSeconds))
                                .request(Duration.ofSeconds(readTimeoutSeconds))
                                .build()))
                .toolCallingManager(ToolCallingManager.builder()
                        .observationRegistry(observationRegistry)
                        .build())
                .build();

        log.info("Coding LLM configured via LiteLLM gateway: model={} baseUrl={}", model, baseUrl);

        OptionsConverter gatewayOptionsConverter =
                (llmOptions, modelName) -> convertToGatewayOptions(readTimeoutSeconds, llmOptions, modelName);

        return CodingLlmServices.create(model, "LiteLLM", chatModel, gatewayOptionsConverter);
    }

    static org.springframework.ai.chat.prompt.ChatOptions convertToGatewayOptions(long readTimeoutSeconds, LlmOptions options, String model) {
        return OpenAiChatOptions.builder()
                .model(model)
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .build();
    }
}
