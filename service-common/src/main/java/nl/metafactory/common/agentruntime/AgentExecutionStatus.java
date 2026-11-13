package nl.metafactory.common.agentruntime;

import java.time.Instant;

public record AgentExecutionStatus(String executionId, State state, Instant startedAt, Instant finishedAt,
                                   String reason) {

    public enum State { RUNNING, COMPLETED, FAILED }
}
