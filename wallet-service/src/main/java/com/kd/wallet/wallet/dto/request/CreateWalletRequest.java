package com.kd.wallet.wallet.dto.request;

import jakarta.validation.constraints.Size;

public record CreateWalletRequest(
		Long userId,

		@Size(max = 32, message = "Customer id must be at most 32 characters")
		String customerId,

		@Size(max = 120, message = "Account name must be at most 120 characters")
		String accountName,

		@Size(max = 20, message = "Account number must be at most 20 characters")
		String accountNumber,

		@Size(max = 10, message = "Currency must be at most 10 characters")
		String currency
) {
}
