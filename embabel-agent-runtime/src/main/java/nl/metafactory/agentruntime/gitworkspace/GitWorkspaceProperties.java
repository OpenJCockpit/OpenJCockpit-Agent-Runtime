package nl.metafactory.agentruntime.gitworkspace;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("openjcockpit.git-workspace")
public class GitWorkspaceProperties {

    private String engineA2aClientId = "openjcockpit-a2a";

    private String gatewayBaseUrl;

    private String gatewayApiKey;

    private String serverId = "openjcockpit_git";

    private Duration timeout = Duration.ofSeconds(120);

    public String getEngineA2aClientId() { return engineA2aClientId; }
    public void setEngineA2aClientId(String engineA2aClientId) { this.engineA2aClientId = engineA2aClientId; }
    public String getGatewayBaseUrl() { return gatewayBaseUrl; }
    public void setGatewayBaseUrl(String gatewayBaseUrl) { this.gatewayBaseUrl = gatewayBaseUrl; }
    public String getGatewayApiKey() { return gatewayApiKey; }
    public void setGatewayApiKey(String gatewayApiKey) { this.gatewayApiKey = gatewayApiKey; }
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
}
