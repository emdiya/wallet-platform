package com.kd.wallet.transfer.dto.request;

import java.math.BigDecimal;

public record ReserveHoldWalletRequest(
		String accountNumber,
		String holdId,
		String operationId,
		BigDecimal amount,
		String purpose
) {
}
