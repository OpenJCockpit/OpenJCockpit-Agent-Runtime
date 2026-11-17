package nl.metafactory.agentruntime.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;

class OnAiProviderCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var accepted = metadata.getAnnotations().get(ConditionalOnAiProvider.class)
                .getEnumArray("value", AiProvider.class);
        return Arrays.asList(accepted).contains(AiProvider.of(context.getEnvironment()));
    }
}
