package nl.metafactory.common.agentruntime;

public record WorkflowRunDescriptor(String workflowId, String workflowRunId, GitWorkspace gitWorkspace) {

    public record GitWorkspace(String workspaceKey, String repositoryUrl, String branch) {
    }
}
