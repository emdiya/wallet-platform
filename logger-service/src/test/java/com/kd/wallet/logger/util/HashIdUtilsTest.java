package com.kd.wallet.logger.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashIdUtilsTest {

	@Test
	void shouldNormalizeHashId() {
		assertEquals("abcdef1234567890", HashIdUtils.normalizeNullable("abcdef1234567890"));
		assertNull(HashIdUtils.normalizeNullable(" "));
	}

	@Test
	void shouldRejectInvalidHashId() {
		assertThrows(IllegalArgumentException.class, () -> HashIdUtils.normalizeNullable("trace-123"));
	}

}
