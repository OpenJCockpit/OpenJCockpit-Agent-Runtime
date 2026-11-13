package nl.metafactory.agentruntime.config;

import nl.metafactory.common.agentruntime.AgentRuntimeApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = "spring.config.location=file:src/main/resources/")
@ActiveProfiles({"dev", "mock"})
@AutoConfigureMockMvc
class MockProfileAgentInvocationTest {

    static final String SEND = """
            {"jsonrpc":"2.0","id":"1","method":"message/send","params":{"message":{"kind":"message","role":"user",
             "messageId":"m-1","parts":[{"kind":"text","text":"Write requirements for a password reset."}]}}}""";

    @Autowired MockMvc mockMvc;

    @Test
    void aSeededAgentAnswersThroughTheA2AEntryPointWithTheMockResponse() throws Exception {
        mockMvc.perform(post("/a2a/business-analyst").contentType(MediaType.APPLICATION_JSON).content(SEND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.result.metadata.agentId").value("business-analyst"))
                .andExpect(jsonPath("$.result.parts[0].text")
                        .value("[mock] Mock response to: Write requirements for a password reset."));
    }

    @Test
    void theDefinitionDraftEntryPointReturnsTheStructuredMockDraft() throws Exception {
        mockMvc.perform(post(AgentRuntimeApi.DEFINITION_DRAFT, "skill").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"concept\":\"summarise commits\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("mock-name"))
                .andExpect(jsonPath("$.outputContract").value("mock-outputContract"))
                .andExpect(jsonPath("$.mcpTools[0].description").value("mock-description"));
    }
}
