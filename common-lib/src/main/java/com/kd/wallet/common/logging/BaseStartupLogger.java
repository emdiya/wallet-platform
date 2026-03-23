package com.kd.wallet.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

public abstract class BaseStartupLogger {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BaseLoggerClient loggerClient;
    private final Environment environment;

    protected BaseStartupLogger(BaseLoggerClient loggerClient, Environment environment) {
        this.loggerClient = loggerClient;
        this.environment = environment;
    }

    protected abstract String serviceName();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "unknown"));
        long pid = ProcessHandle.current().pid();
        String details = serviceName() + " is ready on port " + port + " (pid=" + pid + ")";

        log.info("Service started: {}", details);
        loggerClient.info("Service started", details);
    }
}
