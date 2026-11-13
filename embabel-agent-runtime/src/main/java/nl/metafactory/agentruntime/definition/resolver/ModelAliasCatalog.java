package nl.metafactory.agentruntime.definition.resolver;

import java.util.Set;

public record ModelAliasCatalog(Set<String> aliases) {

    public ModelAliasCatalog {
        aliases = Set.copyOf(aliases);
    }

    public boolean contains(String alias) {
        return aliases.contains(alias);
    }
}
