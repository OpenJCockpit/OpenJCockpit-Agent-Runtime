package nl.metafactory.agentruntime.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

final class NotConfiguredChatModel implements ChatModel {

    static final String MESSAGE = "Not configured: AI_PROVIDER=Other has no language model; "
            + "set AI_PROVIDER to OpenAI, Anthropic or LiteLLM";

    private static final Logger log = LoggerFactory.getLogger(NotConfiguredChatModel.class);

    @Override
    public ChatResponse call(Prompt prompt) {
        log.warn(MESSAGE);
        throw new IllegalStateException(MESSAGE);
    }
}
