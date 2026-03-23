package com.kd.wallet.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
		Long id,
		Long userId,
		String customerId,
		String accountName,
		String accountNumber,
		String currency,
		BigDecimal balance,
		LocalDateTime createdAt
) {
}
