package com.kd.wallet.logger.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogLevelUtilsTest {

	@Test
	void shouldNormalizeLevel() {
		assertEquals("INFO", LogLevelUtils.normalize("info"));
	}

	@Test
	void shouldRejectUnknownLevel() {
		assertThrows(IllegalArgumentException.class, () -> LogLevelUtils.normalize("fatal"));
	}

}
