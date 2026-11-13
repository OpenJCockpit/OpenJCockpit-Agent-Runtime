package nl.metafactory.agentruntime.workflowrun;

import nl.metafactory.agentruntime.gitworkspace.GitWorkspaceBinding;
import nl.metafactory.common.agentruntime.WorkflowRunDescriptor;

import java.util.Objects;

public record WorkflowRunScope(String workflowId, String workflowRunId, GitWorkspaceBinding gitWorkspace) {

    public WorkflowRunScope {
        Objects.requireNonNull(workflowRunId, "workflowRunId");
    }

    public static WorkflowRunScope from(WorkflowRunDescriptor descriptor) {
        var ws = descriptor.gitWorkspace();
        return new WorkflowRunScope(descriptor.workflowId(), descriptor.workflowRunId(), ws == null ? null
                : new GitWorkspaceBinding(ws.workspaceKey(), ws.repositoryUrl(), ws.branch()));
    }
}
