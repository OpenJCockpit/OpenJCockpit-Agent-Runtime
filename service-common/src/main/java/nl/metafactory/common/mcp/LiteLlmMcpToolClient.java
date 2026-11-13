package nl.metafactory.common.mcp;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

public class LiteLlmMcpToolClient {

    public record Config(String baseUrl, String apiKey, String serverId, Duration timeout) {
        public boolean configured() {
            return baseUrl != null && !baseUrl.isBlank() && apiKey != null && !apiKey.isBlank()
                    && serverId != null && !serverId.isBlank();
        }

        @Override
        public String toString() {
            return "Config[baseUrl=" + baseUrl + ", apiKey=***, serverId=" + serverId + ", timeout=" + timeout + "]";
        }
    }

    public record ToolResult(boolean error, String text) {
    }

    public static class McpCallException extends RuntimeException {
        public McpCallException(String message) {
            super(message);
        }
    }

    private final Config config;
    private final RestClient rest;
    private final JsonMapper json = JsonMapper.builder().build();

    public LiteLlmMcpToolClient(Config config) {
        this(config, defaultRestClient(config.timeout()));
    }

    LiteLlmMcpToolClient(Config config, RestClient rest) {
        this.config = config;
        this.rest = rest;
    }

    static RestClient defaultRestClient(Duration timeout) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(timeout != null ? timeout : Duration.ofSeconds(180));
        return RestClient.builder().requestFactory(factory).build();
    }

    public boolean configured() {
        return config.configured();
    }

    public ToolResult call(String toolName, Map<String, ?> arguments) {
        if (!configured()) {
            throw new McpCallException("LiteLLM MCP gateway is not configured");
        }
        byte[] body;
        try {
            body = rest.post().uri(config.baseUrl().replaceAll("/+$", "") + "/mcp-rest/tools/call")
                    .header("Authorization", "Bearer " + config.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("server_id", config.serverId(), "name", toolName, "arguments", arguments))
                    .retrieve().body(byte[].class);
        } catch (RestClientResponseException e) {
            throw new McpCallException("MCP tool " + toolName + " via LiteLLM failed with HTTP " + e.getStatusCode().value());
        } catch (RuntimeException e) {
            throw new McpCallException("MCP tool " + toolName + " via LiteLLM is unreachable: " + e.getClass().getSimpleName());
        }
        return parse(body);
    }

    ToolResult parse(byte[] body) {
        JsonNode result = json.readTree(body == null ? new byte[0] : body);
        var text = new StringBuilder();
        for (JsonNode part : result.path("content")) {
            if ("text".equals(part.path("type").asString(""))) {
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(part.path("text").asString(""));
            }
        }
        return new ToolResult(result.path("isError").asBoolean(false), text.toString());
    }
}
