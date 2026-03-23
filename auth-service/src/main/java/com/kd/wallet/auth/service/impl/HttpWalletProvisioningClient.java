package com.kd.wallet.auth.service.impl;

import com.kd.wallet.auth.dto.request.CreateWalletRequest;
import com.kd.wallet.auth.exception.ExternalServiceException;
import com.kd.wallet.auth.logging.LoggerClient;
import com.kd.wallet.auth.service.WalletProvisioningClient;
import com.kd.wallet.common.logging.RequestMetadata;
import com.kd.wallet.common.logging.RequestMetadataContext;
import com.kd.wallet.common.logging.RequestMetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpWalletProvisioningClient implements WalletProvisioningClient {

	private final RestClient restClient;
	private final LoggerClient loggerClient;
	private final String internalApiKey;

	public HttpWalletProvisioningClient(@Value("${wallet-service.base-url:http://localhost:8082}") String baseUrl,
			@Value("${wallet-service.internal-api-key}") String internalApiKey,
			LoggerClient loggerClient) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
		this.internalApiKey = internalApiKey;
		this.loggerClient = loggerClient;
	}

	@Override
	public void createWallet(Long userId, String customerId, String accountName, String accountNumber) {
		try {
			restClient.post()
					.uri("/api/wallets")
					.headers(this::applyMetadataHeaders)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new CreateWalletRequest(userId, customerId, accountName, accountNumber, "USD"))
					.retrieve()
					.toBodilessEntity();
		} catch (Exception exception) {
			loggerClient.error("Wallet provisioning failed",
					"userId=" + userId + ", accountNumber=" + accountNumber + ", reason=" + exception.getMessage());
			throw new ExternalServiceException(
					"Registration requires wallet-service to be available. Unable to create the default USD wallet at this time.");
		}
	}

	private void applyMetadataHeaders(HttpHeaders headers) {
		RequestMetadata metadata = RequestMetadataContext.get();
		if (metadata == null) {
			return;
		}
		if (metadata.traceId() != null && !metadata.traceId().isBlank()) {
			headers.set(RequestMetadataUtils.TRACE_ID_HEADER, metadata.traceId());
		}
		if (metadata.hashId() != null && !metadata.hashId().isBlank()) {
			headers.set(RequestMetadataUtils.HASH_ID_HEADER, metadata.hashId());
		}
		if (metadata.requestId() != null && !metadata.requestId().isBlank()) {
			headers.set(RequestMetadataUtils.REQUEST_ID_HEADER, metadata.requestId());
		}
		headers.set("X-Internal-Api-Key", internalApiKey);
	}
}
