package com.kd.wallet.auth.security;

import com.kd.wallet.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

	@Test
	void shouldGenerateAndParseToken() {
		JwtService jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "jwtSecret", "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZg==");
		ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
		ReflectionTestUtils.invokeMethod(jwtService, "init");

		User user = new User();
		user.setId(5L);
		user.setFullName("Jane Doe");
		user.setPhone("+85512345678");

		String token = jwtService.generateToken(user);

		assertTrue(jwtService.isTokenValid(token));
		assertEquals(5L, jwtService.extractUserId(token));
		assertTrue(jwtService.extractExpiration(token).isAfter(Instant.now()));
	}

}
