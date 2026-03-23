package com.kd.wallet.logger.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceNameUtilsTest {

	@Test
	void shouldNormalizeServiceName() {
		assertEquals("wallet-service", ServiceNameUtils.normalize("wallet-service"));
	}

	@Test
	void shouldRejectInvalidServiceName() {
		assertThrows(IllegalArgumentException.class, () -> ServiceNameUtils.normalize("Wallet Service"));
	}

}
