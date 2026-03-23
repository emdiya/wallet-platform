package com.kd.wallet.common.logging;

public final class RequestMetadataContext {

    private static final ThreadLocal<RequestMetadata> CONTEXT = new ThreadLocal<>();

    private RequestMetadataContext() {
    }

    public static void set(String traceId, String hashId, String requestId) {
        CONTEXT.set(new RequestMetadata(traceId, hashId, requestId));
    }

    public static RequestMetadata get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
