package nl.metafactory.agentruntime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("openjcockpit.agent-runtime")
public class RuntimeApiProperties {

    private String trustedClientId = "openjcockpit-agent-service";

    public String getTrustedClientId() {
        return trustedClientId;
    }

    public void setTrustedClientId(String trustedClientId) {
        this.trustedClientId = trustedClientId;
    }
}
