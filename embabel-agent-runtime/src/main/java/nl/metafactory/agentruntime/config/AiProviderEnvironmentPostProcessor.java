package nl.metafactory.agentruntime.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiProviderEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String SOURCE = "aiProvider";
    static final String MODEL = "openjcockpit.llm.model";
    static final String NOT_CONFIGURED = "not-configured";
    static final String NO_EMBEDDING = "no-embedding";

    private static final List<String> LLM_ROLES = List.of("coding", "best", "cheapest", "agent-chat", "customer-chat");
    private static final List<String> EMBEDDING_ROLES = List.of("best", "cheapest");
    private static final List<String> OTHER_SPRING_AI_MODELS = List.of(
            "image", "moderation", "audio.speech", "audio.transcription");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.matchesProfiles(MockModelConfig.PROFILE)) {
            return;
        }
        var provider = AiProvider.of(environment);
        if (provider == AiProvider.LITELLM) {
            return;
        }
        String model = provider == AiProvider.OTHER ? NOT_CONFIGURED : requiredModel(environment, provider);
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("embabel.models.default-llm", model);
        LLM_ROLES.forEach(role -> overrides.put("embabel.models.llms." + role, model));
        switch (provider) {
            case OPENAI -> overrides.put("spring.ai.openai.chat.model", model);
            case ANTHROPIC -> {
                overrides.put("spring.ai.model.chat", "anthropic");
                overrides.put("spring.ai.anthropic.chat.model", model);
            }
            default -> overrides.put("spring.ai.model.chat", "none");
        }
        if (provider != AiProvider.OPENAI) {
            overrides.put("spring.ai.model.embedding", "none");
            OTHER_SPRING_AI_MODELS.forEach(kind -> overrides.put("spring.ai.model." + kind, "none"));
            overrides.put("embabel.models.default-embedding-model", NO_EMBEDDING);
            EMBEDDING_ROLES.forEach(role -> overrides.put("embabel.models.embedding-services." + role, NO_EMBEDDING));
        }
        var source = new MapPropertySource(SOURCE, overrides);
        var sources = environment.getPropertySources();
        if (sources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, source);
        } else {
            sources.addFirst(source);
        }
    }

    private static String requiredModel(ConfigurableEnvironment environment, AiProvider provider) {
        String model = environment.getProperty(MODEL);
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("AI_MODEL is required when AI_PROVIDER=" + provider.label());
        }
        return model.strip();
    }
}
