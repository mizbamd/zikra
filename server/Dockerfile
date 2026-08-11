# Build from repo root:
#   docker build -f server/Dockerfile .
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src/server
COPY server /src/server
COPY catalog /src/catalog
RUN chmod +x gradlew \
    && ./gradlew installDist --no-daemon --console=plain

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 zikra \
    && useradd --system --uid 10001 --gid zikra --home /app --shell /usr/sbin/nologin zikra
WORKDIR /app
COPY --from=build --chown=zikra:zikra /src/server/build/install/zikra-server/ ./
USER zikra
EXPOSE 8080
ENV HOST=0.0.0.0 \
    PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8080/health || exit 1
ENTRYPOINT ["/app/bin/zikra-server"]
