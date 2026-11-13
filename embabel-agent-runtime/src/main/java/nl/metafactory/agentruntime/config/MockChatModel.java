package nl.metafactory.agentruntime.config;

import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.databind.json.JsonMapper;

final class MockChatModel implements ChatModel {

    static final String SCHEMA_MARKER = "Here is the JSON Schema instance your output must adhere to:";
    private static final String FENCE = "```";

    private final String modelName;
    private final JsonMapper json;
    private final JsonSchemaInstanceGenerator generator;

    MockChatModel(String modelName, JsonMapper json, JsonSchemaInstanceGenerator generator) {
        this.modelName = modelName;
        this.json = json;
        this.generator = generator;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        String contents = prompt.getContents();
        String answer = contents.contains(SCHEMA_MARKER)
                ? structuredAnswer(contents)
                : textAnswer(prompt.getUserMessage().getText());
        return new ChatResponse(List.of(new Generation(new AssistantMessage(answer))));
    }

    private String structuredAnswer(String contents) {
        return json.writeValueAsString(generator.instanceOf(json.readTree(schemaIn(contents))));
    }

    private String textAnswer(String userMessage) {
        return "[" + modelName + "] Mock response to: " + userMessage;
    }

    private static String schemaIn(String contents) {
        int start = contents.indexOf(FENCE, contents.indexOf(SCHEMA_MARKER));
        int end = start < 0 ? -1 : contents.indexOf(FENCE, start + FENCE.length());
        if (end < 0) {
            throw new UnsupportedOperationException("Mock LLM found no fenced JSON schema after the structured-output marker");
        }
        return contents.substring(start + FENCE.length(), end);
    }
}
