package com.kd.wallet.auth.util;

public final class PasswordUtils {

	private PasswordUtils() {
	}

	public static void validateStrength(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}
		if (password.length() < 8 || password.length() > 72) {
			throw new IllegalArgumentException("Password must be between 8 and 72 characters");
		}

		boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
		boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);

		if (!hasUppercase || !hasLowercase || !hasDigit) {
			throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
		}
	}

}
