package com.kd.wallet.logger.dto.response;

import java.time.LocalDateTime;

public record LogResponse(
		Long id,
		String sourceService,
		String level,
		String message,
		String traceId,
		String hashId,
		String details,
		LocalDateTime createdAt
) {
}
