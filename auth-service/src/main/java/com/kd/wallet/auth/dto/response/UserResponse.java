package com.kd.wallet.auth.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String customerId,
		String fullName,
		String accountName,
		String accountNumber,
		String phone,
		boolean hasTpin,
		LocalDateTime createdAt
) {
}
