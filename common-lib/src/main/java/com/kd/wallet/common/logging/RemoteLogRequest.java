package com.kd.wallet.common.logging;

public record RemoteLogRequest(
        String sourceService,
        String level,
        String message,
        String traceId,
        String hashId,
        String details
) {
}
