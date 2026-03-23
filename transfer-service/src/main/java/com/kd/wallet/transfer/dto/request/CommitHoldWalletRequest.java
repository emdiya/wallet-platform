package com.kd.wallet.transfer.dto.request;

public record CommitHoldWalletRequest(
		String holdId,
		String operationId,
		String purpose
) {
}
