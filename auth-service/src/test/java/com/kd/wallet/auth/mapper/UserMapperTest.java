package com.kd.wallet.auth.mapper;

import com.kd.wallet.auth.dto.request.RegisterRequest;
import com.kd.wallet.auth.dto.response.LoginResponse;
import com.kd.wallet.auth.dto.response.UserResponse;
import com.kd.wallet.auth.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserMapperTest {

	private final UserMapper userMapper = new UserMapper();

	@Test
	void shouldMapRegisterRequestToEntity() {
		RegisterRequest request = new RegisterRequest("  Jane Doe  ", "+85512345678", "Password1");

		User user = userMapper.toEntity(request, "+85512345678", "hashed-value");

		assertEquals("Jane Doe", user.getFullName());
		assertEquals("Jane Doe", user.getAccountName());
		assertEquals("+85512345678", user.getPhone());
		assertEquals("hashed-value", user.getPasswordHash());
	}

	@Test
	void shouldMapUserToResponses() {
		User user = new User();
		user.setId(7L);
		user.setFullName("Jane Doe");
		user.setCustomerId("CID20260300000007");
		user.setAccountName("Jane Doe");
		user.setAccountNumber("855010000000007");
		user.setPhone("+85512345678");
		user.setPasswordHash("hashed-value");
		user.setCreatedAt(LocalDateTime.of(2026, 3, 19, 10, 30));

		UserResponse userResponse = userMapper.toUserResponse(user);
		LoginResponse loginResponse = userMapper.toLoginResponse(
				user,
				"token-value",
				LocalDateTime.of(2026, 3, 19, 11, 30)
		);

		assertEquals(7L, userResponse.id());
		assertEquals("CID20260300000007", userResponse.customerId());
		assertEquals("855010000000007", userResponse.accountNumber());
		assertEquals("Jane Doe", userResponse.fullName());
		assertEquals("Jane Doe", loginResponse.accountName());
		assertEquals("+85512345678", loginResponse.phone());
		assertNotNull(loginResponse.authenticatedAt());
		assertEquals("token-value", loginResponse.accessToken());
	}

}
