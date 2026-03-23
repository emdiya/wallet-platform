package com.kd.wallet.logger.util;

public final class TraceIdUtils {

	private TraceIdUtils() {
	}

	public static String normalizeRequired(String traceId) {
		String normalized = normalizeNullable(traceId);
		if (normalized == null) {
			throw new IllegalArgumentException("Trace id is required");
		}
		return normalized;
	}

	public static String normalizeNullable(String traceId) {
		if (traceId == null || traceId.isBlank()) {
			return null;
		}

		String normalized = traceId.trim();
		if (!normalized.matches("^[A-Za-z0-9._:-]{2,100}$")) {
			throw new IllegalArgumentException("Trace id contains unsupported characters");
		}

		return normalized;
	}

}
