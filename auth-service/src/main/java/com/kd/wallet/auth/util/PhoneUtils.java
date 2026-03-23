package com.kd.wallet.auth.util;

public final class PhoneUtils {

	private PhoneUtils() {
	}

	public static String normalize(String phone) {
		if (phone == null || phone.isBlank()) {
			throw new IllegalArgumentException("Phone is required");
		}

		String trimmed = phone.trim().replaceAll("\\s+", "");
		if (!trimmed.matches("^\\+?[0-9]{8,15}$")) {
			throw new IllegalArgumentException("Phone must contain 8 to 15 digits and may start with '+'");
		}

		return trimmed.startsWith("+") ? trimmed : "+" + trimmed;
	}

}
