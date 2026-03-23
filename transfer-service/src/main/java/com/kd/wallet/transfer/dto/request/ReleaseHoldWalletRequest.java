package com.kd.wallet.transfer.dto.request;

public record ReleaseHoldWalletRequest(
		String holdId,
		String operationId,
		String purpose
) {
}
