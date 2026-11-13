package nl.metafactory.agentruntime.ops;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DockerImageContractTest {

    static final Path PROJECT_ROOT = Path.of("..");

    static String read(String file) throws IOException {
        return Files.readString(PROJECT_ROOT.resolve(file));
    }

    @Test
    void theImageIsBuiltFromThisProjectAlone() throws IOException {
        assertThat(read("Dockerfile"))
                .contains("COPY service-common service-common",
                        "COPY embabel-agent-runtime embabel-agent-runtime",
                        "mvn -B --no-transfer-progress package -DskipTests")
                .doesNotContain("services/", "embabel-agent-service");
    }

    @Test
    void theImageCarriesItsDefinitionsAndRunsUnprivileged() throws IOException {
        assertThat(read("Dockerfile"))
                .contains("COPY --from=build /workspace/embabel-agent-runtime/agent-definitions /agent-definitions",
                        "ENV AGENT_DEFINITIONS_LOCAL_ROOT=/agent-definitions",
                        "USER 10001", "EXPOSE 8082");
        assertThat(Files.isDirectory(PROJECT_ROOT.resolve("embabel-agent-runtime/agent-definitions/agents"))).isTrue();
    }

    @Test
    void theHealthcheckProbesReadinessOnTheApplicationPort() throws IOException {
        assertThat(read("Dockerfile")).contains("CMD [\"/usr/local/bin/healthcheck.sh\", \"8082\"]");
        assertThat(read("docker/healthcheck.sh")).contains("/actuator/health/readiness");
        assertThat(read("docker/entrypoint.sh")).contains("exec java", "-jar /app/app.jar");
    }
}
