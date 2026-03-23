package com.kd.wallet.logger.util;

import java.util.Set;

public final class LogLevelUtils {

	private static final Set<String> ALLOWED_LEVELS = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");

	private LogLevelUtils() {
	}

	public static String normalize(String level) {
		if (level == null || level.isBlank()) {
			throw new IllegalArgumentException("Level is required");
		}

		String normalized = level.trim().toUpperCase();
		if (!ALLOWED_LEVELS.contains(normalized)) {
			throw new IllegalArgumentException("Level must be one of TRACE, DEBUG, INFO, WARN, ERROR");
		}

		return normalized;
	}

}
