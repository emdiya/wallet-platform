package com.kd.wallet.common.logging;

public record RequestMetadata(
        String traceId,
        String hashId,
        String requestId
) {
}
