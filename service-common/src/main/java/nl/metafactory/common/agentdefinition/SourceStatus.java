package nl.metafactory.common.agentdefinition;

public enum SourceStatus {
    FOUND,
    NOT_FOUND,
    UNAVAILABLE,
    UNAUTHORIZED,
    INVALID,
    UNSUPPORTED;

    public boolean isFallbackCandidate() {
        return this == NOT_FOUND || this == UNAVAILABLE || this == UNSUPPORTED;
    }
}
