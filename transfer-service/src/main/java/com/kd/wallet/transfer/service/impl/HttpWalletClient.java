package com.kd.wallet.transfer.service.impl;

import com.kd.wallet.common.logging.RequestMetadata;
import com.kd.wallet.common.logging.RequestMetadataContext;
import com.kd.wallet.common.logging.RequestMetadataUtils;
import com.kd.wallet.transfer.dto.request.CommitHoldWalletRequest;
import com.kd.wallet.transfer.dto.request.ReleaseHoldWalletRequest;
import com.kd.wallet.transfer.dto.request.ReserveHoldWalletRequest;
import com.kd.wallet.transfer.dto.request.TopUpWalletRequest;
import com.kd.wallet.transfer.dto.response.ApiEnvelope;
import com.kd.wallet.transfer.dto.response.WalletHoldResponse;
import com.kd.wallet.transfer.dto.response.WalletResponse;
import com.kd.wallet.transfer.exception.TransferFailedException;
import com.kd.wallet.transfer.logging.LoggerClient;
import com.kd.wallet.transfer.service.WalletClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class HttpWalletClient implements WalletClient {

	private static final ParameterizedTypeReference<ApiEnvelope<WalletResponse>> WALLET_RESPONSE_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<ApiEnvelope<WalletHoldResponse>> WALLET_HOLD_RESPONSE_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;
	private final LoggerClient loggerClient;
	private final String internalApiKey;

	public HttpWalletClient(@Value("${wallet.base-url:http://localhost:8082}") String walletBaseUrl,
			@Value("${wallet.internal-api-key}") String internalApiKey,
			LoggerClient loggerClient) {
		this.restClient = RestClient.builder()
				.baseUrl(walletBaseUrl)
				.build();
		this.internalApiKey = internalApiKey;
		this.loggerClient = loggerClient;
	}

	@Override
	public WalletResponse getByAccountNumber(String accountNumber) {
		try {
			ApiEnvelope<WalletResponse> response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/api/wallets/by-account")
							.queryParam("accountNumber", accountNumber)
							.build())
					.headers(this::applyMetadataHeaders)
					.retrieve()
					.body(WALLET_RESPONSE_TYPE);
			return requireBody(response, "Wallet lookup failed");
		} catch (Exception exception) {
			throw walletCallFailed("Wallet lookup failed for accountNumber: " + accountNumber, exception);
		}
	}

	@Override
	public WalletHoldResponse reserveHold(String accountNumber, String holdId, String operationId, BigDecimal amount, String purpose) {
		try {
			ApiEnvelope<WalletHoldResponse> response = restClient.post()
					.uri("/api/wallets/holds/reserve")
					.headers(this::applyMetadataHeaders)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new ReserveHoldWalletRequest(accountNumber, holdId, operationId, amount, purpose))
					.retrieve()
					.body(WALLET_HOLD_RESPONSE_TYPE);
			return requireBody(response, "Wallet hold reserve failed");
		} catch (Exception exception) {
			throw walletCallFailed("Wallet hold reserve failed for holdId: " + holdId, exception);
		}
	}

	@Override
	public WalletHoldResponse commitHold(String holdId, String operationId, String purpose) {
		try {
			ApiEnvelope<WalletHoldResponse> response = restClient.post()
					.uri("/api/wallets/holds/commit")
					.headers(this::applyMetadataHeaders)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new CommitHoldWalletRequest(holdId, operationId, purpose))
					.retrieve()
					.body(WALLET_HOLD_RESPONSE_TYPE);
			return requireBody(response, "Wallet hold commit failed");
		} catch (Exception exception) {
			throw walletCallFailed("Wallet hold commit failed for holdId: " + holdId, exception);
		}
	}

	@Override
	public WalletHoldResponse releaseHold(String holdId, String operationId, String purpose) {
		try {
			ApiEnvelope<WalletHoldResponse> response = restClient.post()
					.uri("/api/wallets/holds/release")
					.headers(this::applyMetadataHeaders)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new ReleaseHoldWalletRequest(holdId, operationId, purpose))
					.retrieve()
					.body(WALLET_HOLD_RESPONSE_TYPE);
			return requireBody(response, "Wallet hold release failed");
		} catch (Exception exception) {
			throw walletCallFailed("Wallet hold release failed for holdId: " + holdId, exception);
		}
	}

	@Override
	public WalletResponse topUp(String accountNumber, String operationId, BigDecimal amount, String purpose) {
		try {
			ApiEnvelope<WalletResponse> response = restClient.post()
					.uri("/api/wallets/top-ups")
					.headers(this::applyMetadataHeaders)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new TopUpWalletRequest(accountNumber, operationId, amount, purpose))
					.retrieve()
					.body(WALLET_RESPONSE_TYPE);
			return requireBody(response, "Wallet top-up failed");
		} catch (Exception exception) {
			throw walletCallFailed("Wallet top-up failed for accountNumber: " + accountNumber, exception);
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

	private <T> T requireBody(ApiEnvelope<T> response, String fallbackMessage) {
		if (response == null || response.data() == null) {
			throw new TransferFailedException(fallbackMessage);
		}
		return response.data();
	}

	private TransferFailedException walletCallFailed(String message, Exception exception) {
		loggerClient.warn("Wallet service call failed", message + ", reason=" + exception.getMessage());
		return new TransferFailedException(message);
	}
}
