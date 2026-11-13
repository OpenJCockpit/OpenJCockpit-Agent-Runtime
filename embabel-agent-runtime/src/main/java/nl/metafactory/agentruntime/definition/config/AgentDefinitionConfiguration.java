package nl.metafactory.agentruntime.definition.config;

import nl.metafactory.agentruntime.config.AgentGatewayModelRegistrar;
import nl.metafactory.agentruntime.definition.binding.AgentTenantBindings;
import nl.metafactory.agentruntime.definition.binding.RemoteAgentTenantBindings;
import nl.metafactory.agentruntime.definition.cache.LastKnownGoodStore;
import nl.metafactory.agentruntime.definition.config.AgentDefinitionProperties.SourceKind;
import nl.metafactory.agentruntime.definition.guardrail.GuardrailExecutor;
import nl.metafactory.agentruntime.definition.guardrail.GuardrailPolicyEnforcer;
import nl.metafactory.agentruntime.definition.litellm.ExternalAgentContentProvider;
import nl.metafactory.agentruntime.definition.litellm.ExternalSkillPackageProvider;
import nl.metafactory.agentruntime.definition.litellm.LiteLlmPolicyGuardrailExecutor;
import nl.metafactory.agentruntime.definition.resolver.AgentDefinitionResolver;
import nl.metafactory.agentruntime.definition.resolver.ConfiguredFallbackPolicy;
import nl.metafactory.agentruntime.definition.resolver.ModelAliasCatalog;
import nl.metafactory.agentruntime.definition.resolver.SourceChainSelector;
import nl.metafactory.agentruntime.definition.resolver.SourceOrder;
import nl.metafactory.agentruntime.definition.runtime.AgentRunService;
import nl.metafactory.agentruntime.definition.runtime.CatalogAgentRuns;
import nl.metafactory.agentruntime.definition.runtime.ConfiguredDefinitionAgent;
import nl.metafactory.agentruntime.definition.skill.LocalSkillInventory;
import nl.metafactory.agentruntime.definition.skill.SkillMarkdownParser;
import nl.metafactory.agentruntime.definition.skill.SkillPackageProvider;
import nl.metafactory.agentruntime.definition.skill.SkillResolver;
import nl.metafactory.agentruntime.definition.source.AgentDefinitionProvider;
import nl.metafactory.agentruntime.definition.source.LocalMarkdownAgentDefinitionProvider;
import nl.metafactory.agentruntime.definition.tool.RegisteredAgentTool;
import nl.metafactory.agentruntime.definition.tool.ToolAccessPolicy;
import nl.metafactory.agentruntime.definition.tool.ToolRegistry;
import nl.metafactory.agentruntime.management.AgentServiceClient;
import nl.metafactory.common.agentdefinition.AgentMarkdownParser;
import nl.metafactory.common.agentdefinition.ConfinedRoot;
import nl.metafactory.common.agentdefinition.LiteLlmHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "openjcockpit.agent-definitions.enabled", havingValue = "true")
public class AgentDefinitionConfiguration {

    @Bean
    static AgentGatewayModelRegistrar agentGatewayModelRegistrar(Environment environment) {
        return new AgentGatewayModelRegistrar(environment);
    }

    @Bean
    AgentMarkdownParser agentMarkdownParser() {
        return new AgentMarkdownParser();
    }

    @Bean
    ConfinedRoot agentDefinitionRoot(AgentDefinitionProperties properties) {
        return new ConfinedRoot(Path.of(properties.getLocalRoot()));
    }

    @Bean
    LocalMarkdownAgentDefinitionProvider localAgentDefinitionProvider(ConfinedRoot root, AgentMarkdownParser parser) {
        return new LocalMarkdownAgentDefinitionProvider(root, parser);
    }

    @Bean
    LocalSkillInventory localSkillInventory(ConfinedRoot root, SkillMarkdownParser parser) {
        return new LocalSkillInventory(root, parser);
    }

    @Bean
    LastKnownGoodStore lastKnownGoodStore(AgentDefinitionProperties properties, AgentMarkdownParser parser) {
        var cache = properties.getCache();
        return cache.getDirectory() == null || cache.getDirectory().isBlank() ? null
                : new LastKnownGoodStore(Path.of(cache.getDirectory()), cache.getTtl(), cache.getMaxEntries(),
                Clock.systemUTC(), parser);
    }

    @Bean
    ToolRegistry agentToolRegistry(ObjectProvider<RegisteredAgentTool> tools) {
        return new ToolRegistry(tools.orderedStream().toList());
    }

    @Bean
    GuardrailPolicyEnforcer guardrailPolicyEnforcer(AgentDefinitionProperties properties, List<GuardrailExecutor> local) {
        var executors = new ArrayList<>(local);
        var client = litellmRegistry(properties.getLitellm());
        properties.getLitellm().getGatewayGuardrails().forEach((id, litellmName) -> {
            if (client == null) {
                throw new IllegalStateException("gateway guardrail " + id + " requires litellm.registry-url and litellm.registry-key");
            }
            executors.add(new LiteLlmPolicyGuardrailExecutor(id, litellmName, client, Clock.systemUTC()));
        });
        return new GuardrailPolicyEnforcer(executors, new HashSet<>(properties.getMandatoryGuardrails()));
    }

    @Bean
    ModelAliasCatalog modelAliasCatalog(Environment environment) {
        Map<String, String> roles = Binder.get(environment)
                .bind("embabel.models.llms", Bindable.mapOf(String.class, String.class)).orElse(Map.of());
        return new ModelAliasCatalog(roles.keySet());
    }

    @Bean
    @Profile("!" + StandaloneConfiguration.PROFILE)
    AgentTenantBindings agentTenantBindings(AgentServiceClient agentService) {
        return new RemoteAgentTenantBindings(agentService);
    }

    @Bean
    AgentDefinitionResolver agentDefinitionResolver(AgentDefinitionProperties properties,
                                                    AgentTenantBindings bindings,
                                                    LocalMarkdownAgentDefinitionProvider local,
                                                    LocalSkillInventory localSkills,
                                                    ObjectProvider<LastKnownGoodStore> lkg,
                                                    ToolRegistry tools, GuardrailPolicyEnforcer guardrails,
                                                    ModelAliasCatalog models) {
        var available = new EnumMap<SourceKind, AgentDefinitionProvider>(SourceKind.class);
        var skillProviders = new ArrayList<SkillPackageProvider>();
        if (properties.getLitellm().getRegistryUrl() != null && !properties.getLitellm().getRegistryUrl().isBlank()) {
            available.put(SourceKind.REMOTE, new ExternalAgentContentProvider());
            skillProviders.add(new ExternalSkillPackageProvider());
        }
        var store = lkg.getIfAvailable();
        if (store != null) {
            available.put(SourceKind.LAST_KNOWN_GOOD, store);
        }
        available.put(SourceKind.LOCAL, local);
        skillProviders.add(localSkills);
        var selector = new SourceChainSelector(SourceOrder.order(available, properties.getSourcePriority()),
                new ConfiguredFallbackPolicy(properties.getFallback()));
        return new AgentDefinitionResolver(bindings, selector, new SkillResolver(skillProviders),
                new ToolAccessPolicy(tools), guardrails, models, Clock.systemUTC(), store);
    }

    @Bean
    AgentRunService agentRunService(AgentDefinitionResolver resolver, ConfiguredDefinitionAgent agent) {
        return new AgentRunService(resolver, agent);
    }

    @Bean
    CatalogAgentRuns catalogAgentRuns(AgentTenantBindings bindings, AgentMarkdownParser parser,
                                      LocalSkillInventory localSkills, ToolRegistry tools,
                                      GuardrailPolicyEnforcer guardrails, ModelAliasCatalog models,
                                      ConfiguredDefinitionAgent agent) {
        return new CatalogAgentRuns(bindings, parser, localSkills, tools, guardrails, models, agent);
    }

    static LiteLlmHttpClient litellmRegistry(AgentDefinitionProperties.LiteLlm litellm) {
        if (litellm.getRegistryUrl() == null || litellm.getRegistryUrl().isBlank()
                || litellm.getRegistryKey() == null || litellm.getRegistryKey().isBlank()) {
            return null;
        }
        return new LiteLlmHttpClient(litellm.getRegistryUrl(), litellm.getRegistryKey(), Duration.ofSeconds(5),
                Duration.ofSeconds(10));
    }
}
