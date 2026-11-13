# OpenJCockpit Agent Runtime

The **execution plane** of [OpenJCockpit](https://github.com/OpenJCockpit/OpenJCockpit): a standalone Spring Boot
application and Docker image (`openjcockpit/embabel-agent-runtime`) that runs configuration-driven agents on the
[Embabel](https://github.com/embabel/embabel-agent) agent platform.

## Modules

| Module                  | Role                                                                                              |
|-------------------------|---------------------------------------------------------------------------------------------------|
| `service-common`        | Shared library: agent definition format, runtime API contracts, OAuth clients, resource-server security. Kept in sync with the OpenJCockpit platform, whose `embabel-agent-service` speaks the same contracts. |
| `embabel-agent-runtime` | The runtime application. Depends on `service-common` only.                                        |

The runtime owns:

- Embabel initialisation (`@EnableAgents`) and `ConfiguredDefinitionAgent`;
- definition resolution (`definition/**`): served definitions and skills in `embabel-agent-runtime/agent-definitions/`
  (shipped in the image at `/agent-definitions`), last-known-good cache, catalogue snapshots sent by the agent service;
- LLM routes (`config/**`: LiteLLM gateway, direct Ollama, OpenAI, agent model aliases) and definition drafts;
- tools (`ToolRegistry`, bind-time allowlist, agent git tools via LiteLLM) and guardrails;
- inbound A2A (`/a2a/{agentId}`, Agent Cards) and the internal execution API (`/internal/v1/**`);
- the in-memory execution registry (one run per execution id, status endpoint).

It has **no database**. Tenant bindings, the scope of active workflow runs and tool-call policy decisions are read
from `embabel-agent-service` (`EMBABEL_AGENT_SERVICE_BASE_URL`) with the runtime's own Keycloak client
(`openjcockpit-agent-runtime`). When the agent service is not configured, every lookup fails closed: the runtime
starts and reports ready, but refuses agent runs, unless it runs with the `standalone` profile.

## Standalone profile

`SPRING_PROFILES_ACTIVE=standalone` runs the seeded agents without the OpenJCockpit platform:

| Concern          | Platform (`local`, `prod`)                      | `standalone`                                                                    |
|------------------|-------------------------------------------------|---------------------------------------------------------------------------------|
| Tenant bindings  | `embabel-agent-service`                         | `openjcockpit.standalone.tenant-bindings` in `application-standalone.yml`: the four delivery agents for tenant `STANDALONE_TENANT` (default `local`) |
| Model aliases    | `agent-chat`, `customer-chat` via LiteLLM       | Same aliases, mapped to the coding model (`OLLAMA_MODEL`)                       |
| Coding LLM route | `gateway` (LiteLLM)                             | `direct` (Ollama at `OLLAMA_BASE_URL`); `LLM_CODING_ROUTE=gateway` switches back |
| Inbound A2A      | Enabled by the profile                          | Enabled; Agent Cards advertise `A2A_PUBLIC_BASE_URL` (default `http://localhost:8082`) |
| Tool calls       | Decided per call by the platform's policy (OPA) | Denied: without a policy decision point every tool call fails closed, and agents answer from their input |

Callers still need a JWT from the configured issuer (`KEYCLOAK_ISSUER`, `KEYCLOAK_JWK_SET_URI`) with the tenant claim
(`tenant_id`). The seeded definitions use the same model aliases, tools and guardrails in both modes, so the platform's
LiteLLM registration keeps matching them.

## Requirements

- JDK 25 and Maven 3.9+
- Docker 23+ (BuildKit) for the image

## Build and test

```bash
mvn clean verify
```

`verify` runs all tests and enforces **100 % JaCoCo line coverage** per module (reports in
`*/target/site/jacoco/index.html`). Always build from `clean`: a stale `target/` can hide a coverage failure.

## Run locally

Without an explicit Spring profile the runtime starts with the `mock` profile (`spring.profiles.default`): a
deterministic in-memory LLM answers every model name and role, so no API key, Ollama, LiteLLM or network is needed.
Text requests are answered with `[mock] Mock response to: <user message>`; structured output (`createObject`) is
generated from the JSON schema Embabel sends, so it always deserialises into the requested type. Embeddings and
streaming fail explicitly. Any explicitly selected profile (`standalone`, `local`, `dev`, `prod`) uses the real
providers instead and never the mock; `prod` refuses to start next to `mock`, `dev` or `local`, and requires
`OPENAI_API_KEY`.

The `dev` profile runs the runtime on a workstation without Keycloak or the agent service: Spring Security
accepts every request as a fixed local identity (`developer`, tenant `local`), and the `standalone` profile is
pulled in for the seeded tenant bindings. The definitions are read from `embabel-agent-runtime/agent-definitions`.
Never use `dev` outside a developer machine. Without `dev` every API call needs a Keycloak-issued JWT.

| Goal                                           | Profiles     |
|------------------------------------------------|--------------|
| Start without any AI provider                  | _(none)_     |
| Invoke agents locally: no Keycloak, no AI      | `dev,mock`   |
| Invoke agents locally against real providers   | `dev`        |

From IntelliJ: open the project folder (Maven import) and run **EmbabelAgentRuntimeApplication (dev, mock LLM)**
(`.run/`). Remove `mock` from its active profiles (and set `OPENAI_API_KEY`, `LLM_CODING_ROUTE`, `OLLAMA_*`)
to use real providers.

From the command line:

```bash
SPRING_PROFILES_ACTIVE=dev,mock mvn -pl embabel-agent-runtime -am spring-boot:run
```

## Run in Docker

```bash
cp .env.example .env
docker build -t openjcockpit/embabel-agent-runtime:local .
docker run --rm --name embabel-agent-runtime -p 8092:8082 --env-file .env \
  --add-host host.docker.internal:host-gateway \
  openjcockpit/embabel-agent-runtime:local
curl http://localhost:8092/actuator/health/readiness
```

The image runs as UID 10001, ships the seeded agent definitions at `/agent-definitions` and has a `HEALTHCHECK`
on the readiness endpoint.

| Mount                                    | Purpose                                                                                       |
|------------------------------------------|-----------------------------------------------------------------------------------------------|
| `/agent-definitions` (read-only)         | Replaces the shipped agent definitions and skills.                                            |
| `/etc/openjcockpit/tls/tls.crt`, `tls.key` | Serve HTTPS on the application port. Both must be present; one alone stops the container.   |
| `/etc/openjcockpit/tls/ca.crt`           | Extra CA certificates trusted for outgoing calls (for example a private root CA).            |

`TLS_CERT_FILE`, `TLS_KEY_FILE`, `TLS_CA_FILE` and `TLS_KEY_PASSWORD` override the TLS defaults. Renewed server
certificates are reloaded without a restart; changes to trusted certificates apply after a restart. A separate
`MANAGEMENT_PORT` stays HTTP unless `MANAGEMENT_SERVER_SSL_ENABLED=true`.

## Configuration

| Variable                                  | Default                                   | Purpose                                                    |
|-------------------------------------------|-------------------------------------------|------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                  | _(none)_                                  | `standalone` without the platform, `local` for the OpenJCockpit compose stack, `prod` for deployments (no fallbacks; placeholders rejected). |
| `STANDALONE_TENANT`                       | `local`                                   | Tenant the `standalone` profile binds the seeded agents to. |
| `OPENAI_API_KEY`                          | _(required)_                              | OpenAI models (`best`, `cheapest`, embeddings).            |
| `KEYCLOAK_ISSUER`, `KEYCLOAK_JWK_SET_URI` | local Keycloak                            | Accepted token issuer and its signing keys.                |
| `LITELLM_BASE_URL`, `LITELLM_VIRTUAL_KEY` | `http://localhost:4000/v1`                | LiteLLM model gateway.                                     |
| `LLM_CODING_ROUTE`                        | `gateway`                                 | `direct` sends the coding model straight to Ollama.        |
| `OLLAMA_BASE_URL`, `OLLAMA_MODEL`         | `http://localhost:11434`, `qwen3.6:27b`   | Coding model.                                              |
| `EMBABEL_AGENT_SERVICE_BASE_URL`          | _(blank: fail closed)_                    | Management plane.                                          |
| `AGENT_RUNTIME_TOKEN_URL`, `AGENT_RUNTIME_CLIENT_SECRET` | _(blank)_                  | Runtime's client-credentials identity.                     |
| `A2A_ENABLED`, `A2A_PUBLIC_BASE_URL`      | `false`, _(blank)_                        | Inbound A2A and the address advertised in Agent Cards.     |
| `AGENT_DEFINITIONS_LOCAL_ROOT`            | `/agent-definitions`                      | Served definitions and skills.                             |

The full set lives in `embabel-agent-runtime/src/main/resources/application*.yml`.

## Quality

- **ISO 25010:** security (deny-by-default, fail-closed lookups, non-root image, no secrets in logs),
  reliability (bounded timeouts and retries, graceful shutdown, readiness probe), maintainability (one module per
  responsibility, the runtime depends only on shared contracts) and testability (100 % line coverage gate).
- **Comments:** code speaks for itself; comments only accompany non-trivial algorithms.

## Use within the OpenJCockpit platform

The platform's `docker-compose.yml` builds this image from a checkout of this repository next to the OpenJCockpit
checkout (`AGENT_RUNTIME_DIR` overrides the location). The platform's backend CI checks out `main` of this repository
to verify its Compose and LiteLLM wiring against the seeded agent definitions, `application-local.yml` and the A2A
protocol version, so changes to those must be matched by a change in the platform.

## CI and releases

| Workflow            | Trigger                    | What it does                                                                  |
|---------------------|----------------------------|-------------------------------------------------------------------------------|
| `ci.yml`            | pull request, push to main | `mvn clean verify`, shellcheck, image build and non-root check; `ci-summary` gate. |
| `release-image.yml` | tag `vX.Y.Z`               | Builds, scans (Trivy, fixable CRITICAL blocks) and pushes `ghcr.io/openjcockpit/embabel-agent-runtime:vX.Y.Z`. |
| `renovate.yml`      | hourly                     | Dependency updates (`RENOVATE_TOKEN` secret).                                 |

## License

Apache License 2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE).
