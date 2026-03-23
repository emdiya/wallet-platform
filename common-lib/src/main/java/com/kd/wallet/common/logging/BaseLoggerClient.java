package com.kd.wallet.common.logging;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public abstract class BaseLoggerClient {

    private final Logger log;
    private final String sourceService;
    private final RestClient restClient;

    protected BaseLoggerClient(Logger log, String sourceService, String baseUrl) {
        this.log = log;
        this.sourceService = sourceService;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void info(String message, String details) {
        send("INFO", message, null, details);
    }

    public void info(String message, String traceId, String details) {
        send("INFO", message, traceId, details);
    }

    public void debug(String message, String traceId, String details) {
        send("DEBUG", message, traceId, details);
    }

    public void debug(String message, String details) {
        send("DEBUG", message, null, details);
    }

    public void trace(String message, String traceId, String details) {
        send("TRACE", message, traceId, details);
    }

    public void trace(String message, String details) {
        send("TRACE", message, null, details);
    }

    public void warn(String message, String traceId, String details) {
        send("WARN", message, traceId, details);
    }

    public void warn(String message, String details) {
        send("WARN", message, null, details);
    }

    public void error(String message, String traceId, String details) {
        send("ERROR", message, traceId, details);
    }

    public void error(String message, String details) {
        send("ERROR", message, null, details);
    }

    private void send(String level, String message, String traceId, String details) {
        RequestMetadata metadata = RequestMetadataContext.get();
        String resolvedTraceId = traceId;
        String resolvedHashId = null;
        if (metadata != null) {
            if (resolvedTraceId == null || resolvedTraceId.isBlank()) {
                resolvedTraceId = metadata.traceId();
            }
            resolvedHashId = metadata.hashId();
        }

        log.info("App log: level={} message={} traceId={} hashId={} details={}",
                level, message, resolvedTraceId, resolvedHashId, details);

        try {
            restClient.post()
                    .uri("/api/logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RemoteLogRequest(sourceService, level, message, resolvedTraceId, resolvedHashId, details))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            log.warn("Failed to send log to logger-service: {}", exception.getMessage());
        }
    }
}
