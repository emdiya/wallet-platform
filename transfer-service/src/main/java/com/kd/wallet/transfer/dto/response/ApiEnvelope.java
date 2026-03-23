package com.kd.wallet.transfer.dto.response;

import java.time.Instant;

public record ApiEnvelope<T>(
		boolean success,
		String message,
		T data,
		Instant timestamp
) {
}
