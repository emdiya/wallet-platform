package com.kd.wallet.auth.mapper;

import com.kd.wallet.auth.dto.request.RegisterRequest;
import com.kd.wallet.auth.dto.response.LoginResponse;
import com.kd.wallet.auth.dto.response.UserResponse;
import com.kd.wallet.auth.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

	public User toEntity(RegisterRequest request, String normalizedPhone, String passwordHash) {
		User user = new User();
		String normalizedFullName = request.fullName().trim();
		user.setFullName(normalizedFullName);
		user.setAccountName(normalizedFullName);
		user.setPhone(normalizedPhone);
		user.setPasswordHash(passwordHash);
		return user;
	}

	public UserResponse toUserResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getCustomerId(),
				user.getFullName(),
				user.getAccountName(),
				user.getAccountNumber(),
				user.getPhone(),
				user.getTpinHash() != null && !user.getTpinHash().isBlank(),
				user.getCreatedAt()
		);
	}

	public LoginResponse toLoginResponse(User user, String accessToken, LocalDateTime expiresAt) {
		return new LoginResponse(
				user.getId(),
				user.getCustomerId(),
				user.getFullName(),
				user.getAccountName(),
				user.getAccountNumber(),
				user.getPhone(),
				user.getTpinHash() != null && !user.getTpinHash().isBlank(),
				user.getCreatedAt(),
				LocalDateTime.now(),
				accessToken,
				"Bearer",
				expiresAt
		);
	}

}
