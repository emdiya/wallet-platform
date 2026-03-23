package com.kd.wallet.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletHoldResponse(
		Long id,
		String holdId,
		String operationId,
		Long walletId,
		Long userId,
		BigDecimal amount,
		String status,
		String purpose,
		LocalDateTime createdAt,
		LocalDateTime processedAt
) {
}
