package com.kd.wallet.wallet.logging;

import com.kd.wallet.common.logging.BaseRequestMetadataFilter;
import com.kd.wallet.common.logging.HttpLogPayloadUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestLoggingFilter extends BaseRequestMetadataFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	private final LoggerClient loggerClient;

	public RequestLoggingFilter(LoggerClient loggerClient) {
		this.loggerClient = loggerClient;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/actuator");
	}

	@Override
	protected void afterRequest(HttpServletRequest request,
			HttpServletResponse response,
			String traceId,
			String hashId,
			long durationMs,
			String requestBody,
			String responseBody) {
		String message = request.getMethod() + " " + request.getRequestURI();
		String details = "status=" + response.getStatus()
				+ ", durationMs=" + durationMs
				+ ", requestBody=" + HttpLogPayloadUtils.sanitizeBody(requestBody)
				+ ", responseBody=" + HttpLogPayloadUtils.sanitizeBody(responseBody);

		log.info("Request: {} {} traceId={} hashId={}", message, details, traceId, hashId);
		loggerClient.info(message, traceId, details);
	}

}
