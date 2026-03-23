package com.kd.wallet.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneUtilsTest {

	@Test
	void shouldNormalizePhone() {
		assertEquals("+85512345678", PhoneUtils.normalize(" +85512345678 "));
		assertEquals("+85512345678", PhoneUtils.normalize("85512345678"));
	}

	@Test
	void shouldRejectInvalidPhone() {
		assertThrows(IllegalArgumentException.class, () -> PhoneUtils.normalize("12-34"));
	}

}
