package com.kd.wallet.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordUtilsTest {

	@Test
	void shouldAcceptStrongPassword() {
		assertDoesNotThrow(() -> PasswordUtils.validateStrength("Password1"));
	}

	@Test
	void shouldRejectWeakPassword() {
		assertThrows(IllegalArgumentException.class, () -> PasswordUtils.validateStrength("password"));
	}

}
