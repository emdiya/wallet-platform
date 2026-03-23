package com.kd.wallet.ledger.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferEvent(
		String eventId,
		String eventType,
		Instant occurredAt,
		String traceId,
		String requestId,
		String referenceNo,
		String fromAccountNumber,
		String toAccountNumber,
		BigDecimal amount,
		String status,
		String holdId,
		String errorCode,
		String errorMessage,
		String purpose
) {
}
