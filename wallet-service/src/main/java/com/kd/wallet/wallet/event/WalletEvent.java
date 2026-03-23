package com.kd.wallet.wallet.event;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletEvent(
		String eventId,
		String eventType,
		Instant occurredAt,
		String traceId,
		String requestId,
		Long walletId,
		Long userId,
		String customerId,
		String accountNumber,
		String currency,
		String operationId,
		String holdId,
		String status,
		BigDecimal amount,
		BigDecimal balance,
		String purpose
) {
}
