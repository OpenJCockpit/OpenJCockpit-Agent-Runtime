package nl.metafactory.agentruntime.a2a;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.common.ai.model.ModelProvider;
import nl.metafactory.agentruntime.definition.binding.AgentTenantBindings;
import nl.metafactory.agentruntime.definition.binding.ConfiguredAgentTenantBindings;
import nl.metafactory.agentruntime.definition.config.StandaloneProperties;
import nl.metafactory.agentruntime.definition.resolver.ModelAliasCatalog;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
        "AGENT_DEFINITIONS_LOCAL_ROOT=agent-definitions",
        "openjcockpit.llm.ollama.model=qwen-standalone-test"
})
@ActiveProfiles("standalone")
@AutoConfigureMockMvc
class A2AStandaloneProfileTest {

    static final String[] SEEDED_AGENTS = {"business-analyst", "solution-architect", "software-developer", "qa-engineer"};

    static final String SEND = """
            {"jsonrpc":"2.0","id":"1","method":"message/send","params":{"message":{"kind":"message","role":"user",
             "messageId":"m-1","parts":[{"kind":"text","text":"Write requirements for a password reset."}]}}}""";

    @MockitoBean ModelProvider modelProvider;
    @MockitoBean Ai ai;

    @Autowired MockMvc mockMvc;
    @Autowired ApplicationContext context;

    @Test
    void bindingsAndModelAliasesComeFromTheProfileInsteadOfTheAgentService() {
        assertThat(context.getBean(AgentTenantBindings.class)).isInstanceOf(ConfiguredAgentTenantBindings.class);
        assertThat(context.getBean(StandaloneProperties.class).tenantBindings()).hasSize(SEEDED_AGENTS.length);
        assertThat(context.getBean(ModelAliasCatalog.class).aliases()).contains("agent-chat", "customer-chat");
        var env = context.getEnvironment();
        assertThat(env.getProperty("embabel.models.llms.agent-chat")).isEqualTo("qwen-standalone-test");
        assertThat(env.getProperty("openjcockpit.llm.coding.route")).isEqualTo("direct");
        assertThat(env.getProperty("openjcockpit.agent-service.base-url")).isBlank();
    }

    @Test
    void everySeededAgentServesItsCardAndAnswersForTenantLocal() throws Exception {
        PromptRunner runner = mock(PromptRunner.class, Answers.RETURNS_SELF);
        when(ai.withLlmByRole("agent-chat")).thenReturn(runner);
        when(runner.generateText(anyString())).thenReturn("REQ-001 ...");
        var caller = jwt().jwt(j -> j.claim("tenant_id", "local"));

        for (String agent : SEEDED_AGENTS) {
            mockMvc.perform(get("/a2a/" + agent + "/.well-known/agent-card.json").with(caller))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("http://localhost:8082/a2a/" + agent))
                    .andExpect(jsonPath("$.version").value("1.2.0"));
            mockMvc.perform(post("/a2a/" + agent).contentType(MediaType.APPLICATION_JSON).content(SEND).with(caller))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.metadata.agentId").value(agent))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    @Test
    void otherTenantsCannotCallTheSeededAgents() throws Exception {
        mockMvc.perform(post("/a2a/business-analyst").contentType(MediaType.APPLICATION_JSON).content(SEND)
                        .with(jwt().jwt(j -> j.claim("tenant_id", "someone-else"))))
                .andExpect(jsonPath("$.error.data.reason").value("TENANT_FORBIDDEN"));
    }
}
