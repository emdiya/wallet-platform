package com.kd.wallet.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMyWalletRequest(
		@NotBlank(message = "Currency is required")
		@Size(max = 10, message = "Currency must be at most 10 characters")
		String currency
) {
}
