package com.kd.wallet.transfer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
		Long id,
		String requestId,
		String referenceNo,
		String fromAccountNumber,
		String toAccountNumber,
		BigDecimal amount,
		String status,
		String holdId,
		String purpose,
		String errorCode,
		String errorMessage,
		LocalDateTime createdAt,
		LocalDateTime completedAt
) {
}
