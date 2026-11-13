package nl.metafactory.agentruntime.definition.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@Validated
@ConfigurationProperties("openjcockpit.agent-definitions")
public class AgentDefinitionProperties {

    public enum SourceKind { REMOTE, LAST_KNOWN_GOOD, LOCAL }

    private boolean enabled = false;

    private String localRoot;

    @NotEmpty
    private List<SourceKind> sourcePriority = new ArrayList<>(
            List.of(SourceKind.REMOTE, SourceKind.LAST_KNOWN_GOOD, SourceKind.LOCAL));

    @NotEmpty
    private String tenantClaim = "tenant_id";

    @NotNull
    private List<String> mandatoryGuardrails = new ArrayList<>();

    @Valid
    @NotNull
    private Fallback fallback = new Fallback();

    @Valid
    @NotNull
    private Cache cache = new Cache();

    @Valid
    @NotNull
    private LiteLlm litellm = new LiteLlm();

    @Valid
    @NotNull
    private A2a a2a = new A2a();

    @AssertTrue(message = "openjcockpit.agent-definitions.local-root is required when the feature is enabled")
    public boolean isLocalRootPresentWhenEnabled() {
        return !enabled || (localRoot != null && !localRoot.isBlank());
    }

    @AssertTrue(message = "openjcockpit.agent-definitions.a2a.enabled requires openjcockpit.agent-definitions.enabled")
    public boolean isA2aOnlyWhenEnabled() {
        return a2a == null || !a2a.isEnabled() || enabled;
    }

    @AssertTrue(message = "openjcockpit.agent-definitions.source-priority must not contain duplicates")
    public boolean isSourcePriorityUnique() {
        return sourcePriority == null || sourcePriority.stream().distinct().count() == sourcePriority.size();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLocalRoot() {
        return localRoot;
    }

    public void setLocalRoot(String localRoot) {
        this.localRoot = localRoot;
    }

    public List<SourceKind> getSourcePriority() {
        return sourcePriority;
    }

    public void setSourcePriority(List<SourceKind> sourcePriority) {
        this.sourcePriority = sourcePriority;
    }

    public String getTenantClaim() {
        return tenantClaim;
    }

    public void setTenantClaim(String tenantClaim) {
        this.tenantClaim = tenantClaim;
    }

    public List<String> getMandatoryGuardrails() {
        return mandatoryGuardrails;
    }

    public void setMandatoryGuardrails(List<String> mandatoryGuardrails) {
        this.mandatoryGuardrails = mandatoryGuardrails;
    }

    public Fallback getFallback() {
        return fallback;
    }

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public LiteLlm getLitellm() {
        return litellm;
    }

    public void setLitellm(LiteLlm litellm) {
        this.litellm = litellm;
    }

    public A2a getA2a() {
        return a2a;
    }

    public void setA2a(A2a a2a) {
        this.a2a = a2a;
    }

    public static class Fallback {

        private boolean onUnavailable = true;

        private boolean onNotFound = false;

        public boolean isOnUnavailable() {
            return onUnavailable;
        }

        public void setOnUnavailable(boolean onUnavailable) {
            this.onUnavailable = onUnavailable;
        }

        public boolean isOnNotFound() {
            return onNotFound;
        }

        public void setOnNotFound(boolean onNotFound) {
            this.onNotFound = onNotFound;
        }
    }

    public static class Cache {

        @NotNull
        private Duration ttl = Duration.ofHours(1);

        private int maxEntries = 256;

        private String directory;

        @AssertTrue(message = "cache.ttl must be positive and cache.max-entries must be > 0")
        public boolean isPositive() {
            return ttl != null && !ttl.isNegative() && !ttl.isZero() && maxEntries > 0;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }
    }

    public static class LiteLlm {

        @NotNull
        private java.util.Map<String, String> gatewayGuardrails = new java.util.LinkedHashMap<>();

        @NotNull
        private List<String> gatewayModelAliases = new ArrayList<>();

        private String modelGatewayUrl;

        private String modelGatewayKey;

        private String registryUrl;

        private String registryKey;

        public String getRegistryUrl() {
            return registryUrl;
        }

        public void setRegistryUrl(String registryUrl) {
            this.registryUrl = registryUrl;
        }

        public String getRegistryKey() {
            return registryKey;
        }

        public void setRegistryKey(String registryKey) {
            this.registryKey = registryKey;
        }

        public java.util.Map<String, String> getGatewayGuardrails() {
            return gatewayGuardrails;
        }

        public void setGatewayGuardrails(java.util.Map<String, String> gatewayGuardrails) {
            this.gatewayGuardrails = gatewayGuardrails;
        }

        public List<String> getGatewayModelAliases() {
            return gatewayModelAliases;
        }

        public void setGatewayModelAliases(List<String> gatewayModelAliases) {
            this.gatewayModelAliases = gatewayModelAliases;
        }

        public String getModelGatewayUrl() {
            return modelGatewayUrl;
        }

        public void setModelGatewayUrl(String modelGatewayUrl) {
            this.modelGatewayUrl = modelGatewayUrl;
        }

        public String getModelGatewayKey() {
            return modelGatewayKey;
        }

        public void setModelGatewayKey(String modelGatewayKey) {
            this.modelGatewayKey = modelGatewayKey;
        }

        @Override
        public String toString() {
            return "LiteLlm{modelGatewayUrl=" + modelGatewayUrl + ", modelGatewayKey="
                    + (modelGatewayKey == null ? "null" : "***") + ", registryUrl=" + registryUrl + ", registryKey="
                    + (registryKey == null ? "null" : "***") + "}";
        }
    }

    public static class A2a {

        private boolean enabled = false;

        private String publicBaseUrl;

        @AssertTrue(message = "a2a.public-base-url is required when a2a.enabled=true")
        public boolean isConfiguredWhenEnabled() {
            return !enabled || (publicBaseUrl != null && !publicBaseUrl.isBlank());
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }
}
