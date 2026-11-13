package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.EmbeddingService;
import com.embabel.common.ai.model.SpringAiEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Profile(MockModelConfig.PROFILE)
public class MockModelConfig {

    public static final String PROFILE = "mock";
    static final String LLM_NAME = "mock";
    static final String EMBEDDING_NAME = "mock-embedding";
    static final String PROVIDER = "Mock";

    private static final Logger log = LoggerFactory.getLogger(MockModelConfig.class);

    @Bean
    SpringAiLlmService mockLlm() {
        log.warn("llm.mock-mode: every LLM call is answered in memory by the deterministic model '{}'", LLM_NAME);
        return new SpringAiLlmService(LLM_NAME, PROVIDER,
                new MockChatModel(LLM_NAME, JsonMapper.builder().build(), new JsonSchemaInstanceGenerator()));
    }

    @Bean
    EmbeddingService mockEmbeddingService() {
        return new SpringAiEmbeddingService(EMBEDDING_NAME, PROVIDER, new UnsupportedEmbeddingModel(), null, null);
    }
}
