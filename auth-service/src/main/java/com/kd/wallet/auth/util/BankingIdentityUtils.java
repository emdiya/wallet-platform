package com.kd.wallet.auth.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class BankingIdentityUtils {

	private static final DateTimeFormatter CUSTOMER_PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final String CUSTOMER_ID_PREFIX = "CID";
	private static final String ACCOUNT_NUMBER_PREFIX = "85501";

	private BankingIdentityUtils() {
	}

	public static String createCustomerId() {
		return CUSTOMER_ID_PREFIX
				+ LocalDateTime.now().format(CUSTOMER_PERIOD_FORMAT)
				+ randomDigits(8);
	}

	public static String createAccountNumber() {
		return ACCOUNT_NUMBER_PREFIX + randomDigits(10);
	}

	private static String randomDigits(int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int index = 0; index < length; index++) {
			builder.append(ThreadLocalRandom.current().nextInt(10));
		}
		return builder.toString();
	}
}
