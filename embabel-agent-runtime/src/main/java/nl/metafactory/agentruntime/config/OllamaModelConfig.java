package nl.metafactory.agentruntime.config;

import com.embabel.agent.api.models.OllamaModels;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.OptionsConverter;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@ConditionalOnProperty(name = "openjcockpit.llm.coding.route", havingValue = "direct")
@Configuration
public class OllamaModelConfig {

    private static final Logger log = LoggerFactory.getLogger(OllamaModelConfig.class);

    @Bean
    SpringAiLlmService ollamaCodingLlm(
            @Value("${openjcockpit.llm.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${openjcockpit.llm.ollama.model:qwen3.6:27b}") String model,
            @Value("${openjcockpit.llm.connect-timeout-seconds:45}") long connectTimeoutSeconds,
            @Value("${openjcockpit.llm.read-timeout-seconds:600}") long readTimeoutSeconds,
            ObservationRegistry observationRegistry) {
        var requestFactory = new ReactorClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        var reactorHttpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(connectTimeoutSeconds).toMillis())
                .responseTimeout(Duration.ofSeconds(readTimeoutSeconds));

        var ollamaApi = OllamaApi.builder()
                .baseUrl(baseUrl)
                .restClientBuilder(RestClient.builder()
                        .requestFactory(requestFactory)
                        .observationRegistry(observationRegistry))
                .webClientBuilder(WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(reactorHttpClient))
                        .observationRegistry(observationRegistry))
                .build();

        var chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(OllamaChatOptions.builder()
                        .model(model)
                        .keepAlive("30m")
                        .build())
                .retryTemplate(boundedOllamaRetryTemplate())
                .toolCallingManager(ToolCallingManager.builder().observationRegistry(observationRegistry).build())
                .modelManagementOptions(ModelManagementOptions.builder()
                        .pullModelStrategy(PullModelStrategy.NEVER)
                        .build())
                .observationRegistry(observationRegistry)
                .build();

        log.info("Ollama coding LLM configured: model={} baseUrl={}", model, baseUrl);

        return CodingLlmServices.create(model, OllamaModels.PROVIDER, chatModel, OLLAMA_OPTIONS_CONVERTER);
    }

    private static RetryTemplate boundedOllamaRetryTemplate() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(0)
                .includes(TransientAiException.class)
                .includes(ResourceAccessException.class)
                .delay(Duration.ofMillis(500))
                .build();
        return new RetryTemplate(retryPolicy);
    }

    static final OptionsConverter OLLAMA_OPTIONS_CONVERTER = OllamaModelConfig::convertToOllamaOptions;

    static org.springframework.ai.chat.prompt.ChatOptions convertToOllamaOptions(LlmOptions options, String model) {
        return OllamaChatOptions.builder()
                .model(model)
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .numPredict(options.getMaxTokens())
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .build();
    }
}
