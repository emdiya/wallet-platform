package com.kd.wallet.wallet.service.impl;

import com.kd.wallet.common.logging.RequestMetadata;
import com.kd.wallet.common.logging.RequestMetadataContext;
import com.kd.wallet.common.logging.RequestMetadataUtils;
import com.kd.wallet.wallet.service.TpinVerificationClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Component
public class HttpTpinVerificationClient implements TpinVerificationClient {

	private final RestClient restClient;

	public HttpTpinVerificationClient(@Value("${auth-service.base-url:http://localhost:8081}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
	}

	@Override
	public void verify(String tpin) {
		restClient.post()
				.uri("/api/auth/tpin/verify")
				.headers(this::applyHeaders)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("tpin", tpin))
				.retrieve()
				.toBodilessEntity();
	}

	private void applyHeaders(HttpHeaders headers) {
		RequestMetadata metadata = RequestMetadataContext.get();
		if (metadata != null) {
			if (metadata.traceId() != null && !metadata.traceId().isBlank()) {
				headers.set(RequestMetadataUtils.TRACE_ID_HEADER, metadata.traceId());
			}
			if (metadata.hashId() != null && !metadata.hashId().isBlank()) {
				headers.set(RequestMetadataUtils.HASH_ID_HEADER, metadata.hashId());
			}
			if (metadata.requestId() != null && !metadata.requestId().isBlank()) {
				headers.set(RequestMetadataUtils.REQUEST_ID_HEADER, metadata.requestId());
			}
		}

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			throw new IllegalStateException("Current request context is not available");
		}
		HttpServletRequest request = attributes.getRequest();
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || authorization.isBlank()) {
			throw new IllegalArgumentException("Authorization header is required");
		}
		headers.set(HttpHeaders.AUTHORIZATION, authorization);
	}
}
