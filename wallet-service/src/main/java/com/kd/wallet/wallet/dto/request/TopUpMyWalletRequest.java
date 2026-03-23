package com.kd.wallet.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TopUpMyWalletRequest(
		@NotBlank(message = "Account number is required")
		@Size(max = 20, message = "Account number must be at most 20 characters")
		String accountNumber,

		@NotBlank(message = "Operation id is required")
		@Size(max = 80, message = "Operation id must be at most 80 characters")
		String operationId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
		BigDecimal amount,

		@Size(max = 255, message = "Purpose must be at most 255 characters")
		String purpose,

		@NotBlank(message = "TPIN is required")
		@Pattern(regexp = "\\d{4,6}", message = "TPIN must be 4 to 6 digits")
		@Size(min = 4, max = 6, message = "TPIN must be 4 to 6 digits")
		String tpin
) {
}
