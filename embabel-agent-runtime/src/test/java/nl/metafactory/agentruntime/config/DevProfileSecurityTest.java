package nl.metafactory.agentruntime.config;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.common.ai.model.ModelProvider;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// the test application.yml shadows the main one (and its `dev: standalone` group), so both are activated here
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = "openjcockpit.agent-definitions.enabled=true")
@ActiveProfiles({"dev", "standalone"})
@AutoConfigureMockMvc
class DevProfileSecurityTest {

    static final String SEND = """
            {"jsonrpc":"2.0","id":"1","method":"message/send","params":{"message":{"kind":"message","role":"user",
             "messageId":"m-1","parts":[{"kind":"text","text":"Write requirements for a password reset."}]}}}""";

    @MockitoBean ModelProvider modelProvider;
    @MockitoBean Ai ai;

    @Autowired MockMvc mockMvc;
    @Autowired ApplicationContext context;

    @Test
    void theKeycloakChainIsReplacedByAFixedLocalIdentity() {
        assertThat(context.getBeanNamesForType(SecurityConfig.class)).isEmpty();
        Jwt identity = context.getBean(Jwt.class);
        assertThat(identity.getSubject()).isEqualTo("developer");
        assertThat(identity.getClaimAsString("tenant_id")).isEqualTo("local");
        assertThat(identity.getClaimAsString("azp")).isEqualTo("openjcockpit-agent-service");
        assertThat(context.getEnvironment().getProperty("openjcockpit.agent-definitions.local-root"))
                .isEqualTo("agent-definitions");
    }

    @Test
    void agentsAnswerWithoutAToken() throws Exception {
        PromptRunner runner = mock(PromptRunner.class, Answers.RETURNS_SELF);
        when(ai.withLlmByRole("agent-chat")).thenReturn(runner);
        when(runner.generateText(anyString())).thenReturn("REQ-001 ...");

        mockMvc.perform(get("/a2a/business-analyst/.well-known/agent-card.json")).andExpect(status().isOk());
        mockMvc.perform(post("/a2a/business-analyst").contentType(MediaType.APPLICATION_JSON).content(SEND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.metadata.agentId").value("business-analyst"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void theMainApplicationYamlGroupsDevWithStandalone() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource(Path.of("src", "main", "resources", "application.yml")));
        assertThat(yaml.getObject().getProperty("spring.profiles.group.dev")).isEqualTo("standalone");
    }
}
