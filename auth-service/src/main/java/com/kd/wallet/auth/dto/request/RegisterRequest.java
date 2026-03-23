package com.kd.wallet.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Full name is required")
		@Size(max = 120, message = "Full name must be at most 120 characters")
		String fullName,

		@NotBlank(message = "Phone is required")
		@Size(max = 30, message = "Phone must be at most 30 characters")
		String phone,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
		String password
) {
}
