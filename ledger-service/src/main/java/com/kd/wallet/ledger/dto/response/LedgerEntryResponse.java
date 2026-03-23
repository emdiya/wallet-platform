package com.kd.wallet.ledger.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record LedgerEntryResponse(
		Long id,
		String eventId,
		String sourceTopic,
		String eventType,
		String aggregateType,
		String aggregateKey,
		String requestId,
		String traceId,
		String referenceNo,
		Long walletId,
		Long userId,
		String customerId,
		String accountNumber,
		String fromAccountNumber,
		String toAccountNumber,
		String currency,
		String operationId,
		String holdId,
		String status,
		BigDecimal amount,
		BigDecimal balance,
		String purpose,
		String errorCode,
		String errorMessage,
		Instant occurredAt,
		LocalDateTime createdAt,
		String payload
) {
}
