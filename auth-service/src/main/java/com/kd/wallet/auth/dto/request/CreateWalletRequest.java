package com.kd.wallet.auth.dto.request;

public record CreateWalletRequest(
		Long userId,
		String customerId,
		String accountName,
		String accountNumber,
		String currency
) {
}
