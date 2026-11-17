package nl.metafactory.agentruntime.config;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AiProvider {

    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    LITELLM("LiteLLM"),
    OTHER("Other");

    public static final String PROPERTY = "openjcockpit.llm.provider";

    private final String label;

    AiProvider(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static AiProvider of(Environment environment) {
        return of(environment.getProperty(PROPERTY));
    }

    public static AiProvider of(String value) {
        if (value == null || value.isBlank()) {
            return LITELLM;
        }
        return Arrays.stream(values())
                .filter(provider -> provider.label.equalsIgnoreCase(value.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AI_PROVIDER must be one of "
                        + Arrays.stream(values()).map(AiProvider::label).collect(Collectors.joining(", "))
                        + ", not: " + value));
    }
}
