# -------- Build stage --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY auth-service /workspace/auth-service
COPY ledger-service /workspace/ledger-service
COPY transfer-service /workspace/transfer-service
COPY wallet-service /workspace/wallet-service

RUN set -e; \
  for svc in auth-service ledger-service transfer-service wallet-service; do \
    chmod +x "/workspace/$svc/mvnw"; \
    (cd "/workspace/$svc" && ./mvnw -q -DskipTests package); \
  done; \
  mkdir -p /artifacts; \
  for svc in auth-service ledger-service transfer-service wallet-service; do \
    jar="$(find "/workspace/$svc/target" -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*.original' -print -quit)"; \
    test -n "$jar"; \
    cp "$jar" "/artifacts/$svc.jar"; \
  done

# -------- Run stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -ms /bin/bash appuser

COPY --from=build /artifacts/auth-service.jar /app/auth-service.jar
COPY --from=build /artifacts/ledger-service.jar /app/ledger-service.jar
COPY --from=build /artifacts/transfer-service.jar /app/transfer-service.jar
COPY --from=build /artifacts/wallet-service.jar /app/wallet-service.jar
COPY --chmod=0755 start-services.sh /app/start-services.sh

USER appuser

EXPOSE 8081 8082 8083 8084
ENTRYPOINT ["/app/start-services.sh"]
