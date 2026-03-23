package com.kd.wallet.logger.util;

public final class ServiceNameUtils {

	private ServiceNameUtils() {
	}

	public static String normalize(String sourceService) {
		if (sourceService == null || sourceService.isBlank()) {
			throw new IllegalArgumentException("Source service is required");
		}

		String normalized = sourceService.trim().toLowerCase();
		if (!normalized.matches("^[a-z0-9-]{2,80}$")) {
			throw new IllegalArgumentException("Source service must contain only lowercase letters, digits, and hyphens");
		}

		return normalized;
	}

}
