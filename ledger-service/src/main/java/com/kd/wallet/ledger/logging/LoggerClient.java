package com.kd.wallet.ledger.logging;

import com.kd.wallet.common.logging.BaseLoggerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggerClient extends BaseLoggerClient {

	private static final Logger log = LoggerFactory.getLogger(LoggerClient.class);

	public LoggerClient(@Value("${logger-service.base-url:http://localhost:8085}") String baseUrl) {
		super(log, "ledger-service", baseUrl);
	}

}
