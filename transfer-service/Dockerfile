# -------- Build stage --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven config first for cache
COPY pom.xml /app/
COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src /app/src

RUN chmod +x /app/mvnw && /app/mvnw -q -DskipTests package && \
  jar="$(ls /app/target/*.jar | grep -v 'original' | head -n1)" && \
  test -n "$jar" && cp "$jar" /app/app.jar

# -------- Run stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/app.jar /app/app.jar

ENV SERVER_PORT=8083
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
