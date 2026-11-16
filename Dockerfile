FROM --platform=$BUILDPLATFORM maven:3.10-eclipse-temurin-25@sha256:047a82fc82c613266cfb1b66fd920be9c8ba51fd67edc68245e4542768f45e36 AS build
WORKDIR /workspace
COPY pom.xml .
COPY service-common service-common
COPY embabel-agent-runtime embabel-agent-runtime
RUN --mount=type=cache,target=/root/.m2 mvn -B --no-transfer-progress package -DskipTests

FROM eclipse-temurin:25-jre@sha256:a640968ae55027b322cedd5f8228b01a84baff0b9e9360e47601c6a111ba1699
WORKDIR /app
RUN useradd --system --uid 10001 --home-dir /app --shell /usr/sbin/nologin runtime
COPY --from=build /workspace/embabel-agent-runtime/target/embabel-agent-runtime-*.jar app.jar
COPY --from=build /workspace/embabel-agent-runtime/agent-definitions /agent-definitions
COPY --chmod=0755 docker/entrypoint.sh docker/healthcheck.sh /usr/local/bin/
USER 10001
EXPOSE 8082
ENV AGENT_DEFINITIONS_LOCAL_ROOT=/agent-definitions
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD ["/usr/local/bin/healthcheck.sh", "8082"]
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
