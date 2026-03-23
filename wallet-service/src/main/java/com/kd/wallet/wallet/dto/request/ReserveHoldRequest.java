package com.kd.wallet.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ReserveHoldRequest(
		@NotBlank(message = "Account number is required")
		@Size(max = 20, message = "Account number must be at most 20 characters")
		String accountNumber,

		@NotBlank(message = "Hold id is required")
		@Size(max = 80, message = "Hold id must be at most 80 characters")
		String holdId,

		@NotBlank(message = "Operation id is required")
		@Size(max = 80, message = "Operation id must be at most 80 characters")
		String operationId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
		BigDecimal amount,

		@Size(max = 255, message = "Purpose must be at most 255 characters")
		String purpose
) {
}
