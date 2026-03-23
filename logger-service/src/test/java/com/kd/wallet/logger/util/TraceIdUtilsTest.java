package com.kd.wallet.logger.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraceIdUtilsTest {

	@Test
	void shouldNormalizeNullableTraceId() {
		assertEquals("trace-123", TraceIdUtils.normalizeNullable(" trace-123 "));
		assertNull(TraceIdUtils.normalizeNullable(" "));
	}

	@Test
	void shouldRejectInvalidTraceId() {
		assertThrows(IllegalArgumentException.class, () -> TraceIdUtils.normalizeNullable("trace id"));
	}

}
