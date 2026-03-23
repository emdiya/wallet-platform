package com.kd.wallet.common.logging;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public final class RequestMetadataUtils {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String HASH_ID_HEADER = "X-Hash-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private RequestMetadataUtils() {
    }

    public static String traceIdOrGenerate(String incomingTraceId) {
        if (incomingTraceId != null && !incomingTraceId.isBlank()) {
            return incomingTraceId.trim();
        }
        return UUID.randomUUID().toString();
    }

    public static String requestIdOrGenerate(String incomingRequestId) {
        if (incomingRequestId != null && !incomingRequestId.isBlank()) {
            return incomingRequestId.trim();
        }
        return UUID.randomUUID().toString();
    }

    public static String createHashId(String method, String path, String traceId) {
        String payload = method + "|" + path + "|" + traceId + "|" + Instant.now().toEpochMilli();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
