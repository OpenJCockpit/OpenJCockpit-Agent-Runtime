package nl.metafactory.common.mcp;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GitMcpGateway {

    public record Result(boolean success, String message, String branch, String commitHash) {
    }

    public record FileContent(boolean success, String message, String path, String content) {
    }

    public record FileList(boolean success, String message, List<String> files) {
    }

    private final LiteLlmMcpToolClient client;
    private final JsonMapper json = JsonMapper.builder().build();

    public GitMcpGateway(LiteLlmMcpToolClient client) {
        this.client = client;
    }

    public boolean configured() {
        return client.configured();
    }

    public Result createBranch(String repositoryUrl, String baseBranch, String newBranch, String workspaceKey) {
        return result(call("git_create_branch", args("repositoryUrl", repositoryUrl, "baseBranch", baseBranch,
                "newBranch", newBranch, "workspaceKey", workspaceKey)));
    }

    public Result writeFile(String repositoryUrl, String path, String content, String workspaceKey) {
        return result(call("git_write_file", args("repositoryUrl", repositoryUrl, "path", path,
                "content", content, "workspaceKey", workspaceKey)));
    }

    public FileContent readFile(String repositoryUrl, String path, String workspaceKey) {
        JsonNode node = call("git_read_file", args("repositoryUrl", repositoryUrl, "path", path, "workspaceKey", workspaceKey));
        return new FileContent(node.path("success").asBoolean(false), node.path("message").asString(""),
                node.path("path").asString(null), node.path("content").asString(null));
    }

    public FileList listFiles(String repositoryUrl, String subdirectory, String workspaceKey) {
        JsonNode node = call("git_list_files", args("repositoryUrl", repositoryUrl, "subdirectory", subdirectory,
                "workspaceKey", workspaceKey));
        var files = new ArrayList<String>();
        node.path("files").forEach(f -> files.add(f.asString()));
        return new FileList(node.path("success").asBoolean(false), node.path("message").asString(""), List.copyOf(files));
    }

    public Result commit(String repositoryUrl, String message, String authorName, String authorEmail, String workspaceKey) {
        return result(call("git_commit", args("repositoryUrl", repositoryUrl, "message", message,
                "authorName", authorName, "authorEmail", authorEmail, "workspaceKey", workspaceKey)));
    }

    public Result push(String repositoryUrl, String branch, String workspaceKey) {
        return result(call("git_push", args("repositoryUrl", repositoryUrl, "branch", branch, "workspaceKey", workspaceKey)));
    }

    private JsonNode call(String tool, Map<String, String> arguments) {
        var outcome = client.call(tool, arguments);
        if (outcome.error()) {
            throw new LiteLlmMcpToolClient.McpCallException("MCP tool " + tool + " returned an error");
        }
        try {
            return json.readTree(outcome.text());
        } catch (RuntimeException e) {
            throw new LiteLlmMcpToolClient.McpCallException("MCP tool " + tool + " returned no JSON result");
        }
    }

    private static Result result(JsonNode node) {
        return new Result(node.path("success").asBoolean(false), node.path("message").asString(""),
                node.path("branch").asString(null), node.path("commitHash").asString(null));
    }

    private static Map<String, String> args(String... keyValues) {
        var map = new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (keyValues[i + 1] != null) {
                map.put(keyValues[i], keyValues[i + 1]);
            }
        }
        return map;
    }
}
