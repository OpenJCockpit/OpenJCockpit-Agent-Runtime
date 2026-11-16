package nl.metafactory.agentruntime.config;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.common.ai.model.ModelProvider;
import nl.metafactory.agentruntime.management.AgentServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "ENABLE_KEYCLOAK=false",
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions",
        "openjcockpit.agent-definitions.enabled=true",
        "openjcockpit.agent-service.base-url=http://embabel-agent-service.invalid:8081"})
@ActiveProfiles("standalone")
@AutoConfigureMockMvc
class KeycloakDisabledSecurityTest {

    static final String SEND = """
            {"jsonrpc":"2.0","id":"1","method":"message/send","params":{"message":{"kind":"message","role":"user",
             "messageId":"m-1","parts":[{"kind":"text","text":"Write requirements for a password reset."}]}}}""";

    @MockitoBean ModelProvider modelProvider;
    @MockitoBean Ai ai;

    @Autowired MockMvc mockMvc;
    @Autowired ApplicationContext context;

    @Test
    void withoutKeycloakEveryRequestIsPermittedAsTheLocalIdentity() throws Exception {
        assertThat(context.getBeanNamesForType(SecurityConfig.class)).isEmpty();
        assertThat(context.getBean(Jwt.class).getClaimAsString("tenant_id")).isEqualTo("local");
        PromptRunner runner = mock(PromptRunner.class, Answers.RETURNS_SELF);
        when(ai.withLlmByRole("agent-chat")).thenReturn(runner);
        when(runner.generateText(anyString())).thenReturn("REQ-001 ...");

        mockMvc.perform(get("/a2a/business-analyst/.well-known/agent-card.json")).andExpect(status().isOk());
        mockMvc.perform(post("/a2a/business-analyst").contentType(MediaType.APPLICATION_JSON).content(SEND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.metadata.agentId").value("business-analyst"));
    }

    @Test
    void withoutKeycloakTheAgentServiceIsNeverCalled() {
        assertThat(context.getBean(AgentServiceClient.class).configured()).isFalse();
    }

    @Test
    void productionRefusesToRunWithoutKeycloak() {
        var production = new MockEnvironment();
        production.setActiveProfiles("prod");
        assertThatThrownBy(() -> new KeycloakDisabledSecurityConfig(production))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SecurityConfig.KEYCLOAK_ENABLED);

        var local = new MockEnvironment();
        local.setActiveProfiles("local");
        assertThatCode(() -> new KeycloakDisabledSecurityConfig(local)).doesNotThrowAnyException();
    }
}
