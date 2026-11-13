package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.DefaultOptionsConverter;
import com.embabel.common.ai.model.EmbeddingService;
import com.embabel.common.ai.model.PricingModel;
import com.embabel.common.ai.model.SpringAiEmbeddingService;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Configuration
@Profile("!" + MockModelConfig.PROFILE)
public class OpenAiModelConfig {

    @Bean
    OpenAiHttpClientBuilderCustomizer openAiHttpClientBuilderCustomizer(
            @Value("${openjcockpit.llm.connect-timeout-seconds:45}") long connectTimeoutSeconds,
            @Value("${openjcockpit.llm.read-timeout-seconds:600}") long readTimeoutSeconds) {
        return builder -> builder.timeout(com.openai.core.Timeout.builder()
                .connect(Duration.ofSeconds(connectTimeoutSeconds))
                .read(Duration.ofSeconds(readTimeoutSeconds))
                .request(Duration.ofSeconds(readTimeoutSeconds))
                .build());
    }

    @Bean
    SpringAiLlmService gpt41Mini(OpenAiChatModel openAiChatModel, ObservationRegistry observationRegistry) {
        return new SpringAiLlmService(
                "gpt-4.1-mini",
                "OpenAI",
                openAiChatModel,
                DefaultOptionsConverter.INSTANCE,
                LocalDate.of(2024, 6, 1),
                List.of(),
                PricingModel.usdPer1MTokens(0.40, 1.60));
    }

    @Bean
    EmbeddingService textEmbedding3Small(OpenAiEmbeddingModel openAiEmbeddingModel) {
        return new SpringAiEmbeddingService(
                "text-embedding-3-small",
                "OpenAI",
                openAiEmbeddingModel,
                null,
                null);
    }
}
