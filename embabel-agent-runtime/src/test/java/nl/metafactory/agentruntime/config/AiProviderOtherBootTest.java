package nl.metafactory.agentruntime.config;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "spring.config.location=file:src/main/resources/",
        "AI_PROVIDER=Other",
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions"
})
@ActiveProfiles("local")
class AiProviderOtherBootTest {

    @Autowired ApplicationContext context;
    @Autowired Ai ai;

    @Test
    void everyRoleResolvesToTheNotConfiguredModelWhichLogsAndFails(CapturedOutput output) {
        var llmNames = Arrays.stream(context.getBeanNamesForType(SpringAiLlmService.class))
                .map(name -> context.getBean(name, SpringAiLlmService.class).getName())
                .toList();
        assertThat(llmNames).containsExactly(AiProviderEnvironmentPostProcessor.NOT_CONFIGURED);

        assertThatThrownBy(() -> ai.withLlmByRole("agent-chat").generateText("hello"))
                .hasStackTraceContaining("Not configured");
        assertThat(output).contains(NotConfiguredChatModel.MESSAGE);
    }
}
