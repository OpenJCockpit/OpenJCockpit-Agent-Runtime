package nl.metafactory.agentruntime.definition.model;

public enum ResolutionFailure {
    NOT_FOUND,
    UNAVAILABLE,
    UNAUTHORIZED,
    INVALID,
    UNSUPPORTED,
    TENANT_FORBIDDEN,
    SKILL_MISSING,
    TOOL_DENIED,
    GUARDRAIL_MISSING,
    POLICY_DENIED
}
