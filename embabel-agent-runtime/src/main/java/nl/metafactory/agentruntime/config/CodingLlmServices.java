package nl.metafactory.agentruntime.config;

import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.embabel.common.ai.model.OptionsConverter;
import com.embabel.common.ai.model.PricingModel;
import com.embabel.common.ai.prompt.PromptContributor;
import org.springframework.ai.chat.model.ChatModel;

import java.time.LocalDate;
import java.util.List;

final class CodingLlmServices {

    static final PromptContributor NO_THINK_PROMPT_CONTRIBUTOR = PromptContributor.fixed("/no_think");

    private CodingLlmServices() {
    }

    static SpringAiLlmService create(String model, String provider, ChatModel chatModel,
                                     OptionsConverter optionsConverter) {
        return new SpringAiLlmService(
                model,
                provider,
                chatModel,
                optionsConverter,
                LocalDate.of(2025, 4, 1),
                List.of(NO_THINK_PROMPT_CONTRIBUTOR),
                PricingModel.usdPer1MTokens(0.0, 0.0));
    }
}
