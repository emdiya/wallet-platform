package com.kd.wallet.transfer.dto.request;

import java.math.BigDecimal;

public record TopUpWalletRequest(
		String accountNumber,
		String operationId,
		BigDecimal amount,
		String purpose
) {
}
