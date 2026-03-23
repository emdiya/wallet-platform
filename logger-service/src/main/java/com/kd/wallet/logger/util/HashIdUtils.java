package com.kd.wallet.logger.util;

public final class HashIdUtils {

	private HashIdUtils() {
	}

	public static String normalizeRequired(String hashId) {
		String normalized = normalizeNullable(hashId);
		if (normalized == null) {
			throw new IllegalArgumentException("Hash id is required");
		}
		return normalized;
	}

	public static String normalizeNullable(String hashId) {
		if (hashId == null || hashId.isBlank()) {
			return null;
		}

		String normalized = hashId.trim().toLowerCase();
		if (!normalized.matches("^[a-f0-9]{8,64}$")) {
			throw new IllegalArgumentException("Hash id must be a lowercase hex string");
		}
		return normalized;
	}

}
