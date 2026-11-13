package nl.metafactory.agentruntime.definition.model;

import java.util.Objects;

public class AgentResolutionException extends RuntimeException {

    private final ResolutionFailure failure;

    public AgentResolutionException(ResolutionFailure failure, String message) {
        super(message);
        this.failure = Objects.requireNonNull(failure, "failure");
    }

    public ResolutionFailure failure() {
        return failure;
    }
}
