package com.kd.wallet.logger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLogRequest(
		@NotBlank(message = "Source service is required")
		@Size(max = 80, message = "Source service must be at most 80 characters")
		String sourceService,

		@NotBlank(message = "Level is required")
		@Size(max = 10, message = "Level must be at most 10 characters")
		String level,

		@NotBlank(message = "Message is required")
		@Size(max = 1000, message = "Message must be at most 1000 characters")
		String message,

		@Size(max = 100, message = "Trace id must be at most 100 characters")
		String traceId,

		@Size(max = 64, message = "Hash id must be at most 64 characters")
		String hashId,
		String details
) {
}
