package com.kd.wallet.transfer.logging;

import com.kd.wallet.common.logging.BaseStartupLogger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger extends BaseStartupLogger {

	public StartupLogger(LoggerClient loggerClient, Environment environment) {
		super(loggerClient, environment);
	}

	@Override
	protected String serviceName() {
		return "transfer-service";
	}
}
