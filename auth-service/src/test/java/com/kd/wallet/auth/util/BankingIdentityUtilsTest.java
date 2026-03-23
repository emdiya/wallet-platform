package com.kd.wallet.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankingIdentityUtilsTest {

	@Test
	void shouldCreateCustomerIdWithExpectedFormat() {
		String customerId = BankingIdentityUtils.createCustomerId();

		assertEquals(17, customerId.length());
		assertTrue(customerId.startsWith("CID"));
		assertTrue(customerId.substring(3).matches("\\d+"));
	}

	@Test
	void shouldCreateAccountNumberWithExpectedFormat() {
		String accountNumber = BankingIdentityUtils.createAccountNumber();

		assertEquals(15, accountNumber.length());
		assertTrue(accountNumber.startsWith("85501"));
		assertTrue(accountNumber.matches("\\d+"));
	}
}
