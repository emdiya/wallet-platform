package com.kd.wallet.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReleaseHoldRequest(
		@NotBlank(message = "Hold id is required")
		@Size(max = 80, message = "Hold id must be at most 80 characters")
		String holdId,

		@NotBlank(message = "Operation id is required")
		@Size(max = 80, message = "Operation id must be at most 80 characters")
		String operationId,

		@Size(max = 255, message = "Purpose must be at most 255 characters")
		String purpose
) {
}
