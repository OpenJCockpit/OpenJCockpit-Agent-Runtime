package nl.metafactory.agentruntime.definition.binding;

import nl.metafactory.common.agentruntime.AgentTenantBinding;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfiguredAgentTenantBindingsTest {

    static AgentTenantBinding binding(String tenant, String agent) {
        return new AgentTenantBinding(tenant, agent, true, "agent-chat", null, false, false,
                Set.of("git.read_file"), Set.of("pii-protection"));
    }

    @Test
    void findsTheBindingOfExactlyTheTenantAndAgent() {
        var bindings = new ConfiguredAgentTenantBindings(List.of(binding("local", "qa-engineer"),
                binding("other", "qa-engineer")));

        assertThat(bindings.find("local", "qa-engineer")).contains(binding("local", "qa-engineer"));
        assertThat(bindings.find("local", "business-analyst")).isEmpty();
        assertThat(bindings.find("someone-else", "qa-engineer")).isEmpty();
    }

    @Test
    void refusesTwoBindingsForTheSameTenantAndAgent() {
        assertThatThrownBy(() -> new ConfiguredAgentTenantBindings(List.of(binding("local", "qa-engineer"),
                binding("local", "qa-engineer")))).isInstanceOf(IllegalStateException.class);
    }
}
