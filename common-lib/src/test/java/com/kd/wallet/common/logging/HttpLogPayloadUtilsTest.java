package com.kd.wallet.common.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpLogPayloadUtilsTest {

	@Test
	void sanitizeBodyMasksSensitiveJsonFields() {
		String body = """
				{"phone":"+85512345678","password":"Secret123","accessToken":"jwt-token","passwordHash":"hashed"}
				""";

		String sanitized = HttpLogPayloadUtils.sanitizeBody(body);

		assertFalse(sanitized.contains("Secret123"));
		assertFalse(sanitized.contains("jwt-token"));
		assertFalse(sanitized.contains("hashed"));
		assertTrue(sanitized.contains("\"password\":\"***\""));
		assertTrue(sanitized.contains("\"accessToken\":\"***\""));
		assertTrue(sanitized.contains("\"passwordHash\":\"***\""));
	}

	@Test
	void sanitizeBodyMasksSensitiveFormFields() {
		String body = "phone=%2B85512345678&password=Secret123&accessToken=jwt-token&token=bearer";

		String sanitized = HttpLogPayloadUtils.sanitizeBody(body);

		assertFalse(sanitized.contains("Secret123"));
		assertFalse(sanitized.contains("jwt-token"));
		assertFalse(sanitized.contains("bearer"));
		assertTrue(sanitized.contains("password=***"));
		assertTrue(sanitized.contains("accessToken=***"));
		assertTrue(sanitized.contains("token=***"));
	}
}
