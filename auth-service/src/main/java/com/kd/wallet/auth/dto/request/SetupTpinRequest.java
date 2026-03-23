package com.kd.wallet.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SetupTpinRequest(
		@NotBlank(message = "TPIN is required")
		@Pattern(regexp = "\\d{4,6}", message = "TPIN must be 4 to 6 digits")
		@Size(min = 4, max = 6, message = "TPIN must be 4 to 6 digits")
		String tpin,

		@NotBlank(message = "Confirm TPIN is required")
		@Pattern(regexp = "\\d{4,6}", message = "Confirm TPIN must be 4 to 6 digits")
		@Size(min = 4, max = 6, message = "Confirm TPIN must be 4 to 6 digits")
		String confirmTpin
) {
}
