package nl.metafactory.agentruntime.gitworkspace;

import java.util.Objects;
import java.util.regex.Pattern;

public record GitWorkspaceBinding(String workspaceKey, String repositoryUrl, String branch) {

    static final Pattern KEY = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$");
    private static final int MAX_BRANCH = 255;

    public GitWorkspaceBinding {
        Objects.requireNonNull(workspaceKey, "workspaceKey");
        if (!KEY.matcher(workspaceKey).matches()) {
            throw new IllegalArgumentException("invalid git workspace key");
        }
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException("a git workspace needs the repository URL");
        }
        if (!isValidBranch(branch)) {
            throw new IllegalArgumentException("invalid git workspace branch");
        }
    }

    static boolean isValidBranch(String branch) {
        return branch != null && !branch.isEmpty() && branch.length() <= MAX_BRANCH
                && !branch.startsWith("-") && !branch.contains("..")
                && branch.codePoints().noneMatch(c -> Character.isWhitespace(c) || c < 0x20 || c == 0x7F);
    }
}
