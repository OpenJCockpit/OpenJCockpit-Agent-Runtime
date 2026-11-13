package nl.metafactory.agentruntime.definition.binding;

import nl.metafactory.common.agentruntime.AgentTenantBinding;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfiguredAgentTenantBindings implements AgentTenantBindings {

    private record Key(String tenantId, String agentId) {
    }

    private final Map<Key, AgentTenantBinding> bindings;

    public ConfiguredAgentTenantBindings(List<AgentTenantBinding> bindings) {
        this.bindings = bindings.stream().collect(Collectors.toUnmodifiableMap(
                binding -> new Key(binding.tenantId(), binding.agentId()), Function.identity()));
    }

    @Override
    public Optional<AgentTenantBinding> find(String tenantId, String agentId) {
        return Optional.ofNullable(bindings.get(new Key(tenantId, agentId)));
    }
}
