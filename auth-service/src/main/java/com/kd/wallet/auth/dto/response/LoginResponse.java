package com.kd.wallet.auth.dto.response;

import java.time.LocalDateTime;

public record LoginResponse(
		Long id,
		String customerId,
		String fullName,
		String accountName,
		String accountNumber,
		String phone,
		boolean hasTpin,
		LocalDateTime createdAt,
		LocalDateTime authenticatedAt,
		String accessToken,
		String tokenType,
		LocalDateTime expiresAt
) {
}
