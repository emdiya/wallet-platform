package com.kd.wallet.wallet.service.impl;

import com.kd.wallet.common.logging.RequestMetadata;
import com.kd.wallet.common.logging.RequestMetadataContext;
import com.kd.wallet.wallet.entity.Wallet;
import com.kd.wallet.wallet.entity.WalletHold;
import com.kd.wallet.wallet.event.WalletEvent;
import com.kd.wallet.wallet.logging.LoggerClient;
import com.kd.wallet.wallet.service.WalletEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaWalletEventPublisher implements WalletEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaWalletEventPublisher.class);

	private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
	private final LoggerClient loggerClient;
	private final String topicName;

	public KafkaWalletEventPublisher(KafkaTemplate<String, WalletEvent> kafkaTemplate,
			LoggerClient loggerClient,
			@Value("${wallet.kafka.topic:wallet.events}") String topicName) {
		this.kafkaTemplate = kafkaTemplate;
		this.loggerClient = loggerClient;
		this.topicName = topicName;
	}

	@Override
	public void publishWalletCreated(Wallet wallet) {
		publish(wallet.getAccountNumber(), buildEvent("WalletCreated", wallet, null, null, null, wallet.getBalance(), null));
	}

	@Override
	public void publishWalletTopUp(Wallet wallet, String operationId, String purpose) {
		publish(wallet.getAccountNumber(),
				buildEvent("WalletTopUpCompleted", wallet, null, operationId, null, wallet.getBalance(), purpose));
	}

	@Override
	public void publishHoldReserved(Wallet wallet, WalletHold walletHold) {
		publish(wallet.getAccountNumber(),
				buildEvent("WalletHoldReserved", wallet, walletHold, walletHold.getOperationId(),
						walletHold.getAmount(), wallet.getBalance(), walletHold.getPurpose()));
	}

	@Override
	public void publishHoldCommitted(Wallet wallet, WalletHold walletHold, String operationId, String purpose) {
		publish(wallet.getAccountNumber(),
				buildEvent("WalletHoldCommitted", wallet, walletHold, operationId,
						walletHold.getAmount(), wallet.getBalance(), purpose));
	}

	@Override
	public void publishHoldReleased(Wallet wallet, WalletHold walletHold, String operationId, String purpose) {
		publish(wallet.getAccountNumber(),
				buildEvent("WalletHoldReleased", wallet, walletHold, operationId,
						walletHold.getAmount(), wallet.getBalance(), purpose));
	}

	@Override
	public void publish(String key, WalletEvent event) {
		try {
			kafkaTemplate.send(topicName, key, event);
		} catch (Exception exception) {
			log.error("Kafka publish failed for topic={} key={} eventType={}", topicName, key, event.eventType(), exception);
			loggerClient.warn("Wallet event publish failed",
					"topic=" + topicName + ", key=" + key + ", eventType=" + event.eventType()
							+ ", reason=" + exception.getMessage());
		}
	}

	private WalletEvent buildEvent(String eventType,
			Wallet wallet,
			WalletHold walletHold,
			String operationId,
			BigDecimal amount,
			BigDecimal balance,
			String purpose) {
		RequestMetadata metadata = RequestMetadataContext.get();
		return new WalletEvent(
				UUID.randomUUID().toString(),
				eventType,
				Instant.now(),
				metadata == null ? null : metadata.traceId(),
				metadata == null ? null : metadata.requestId(),
				wallet.getId(),
				wallet.getUserId(),
				wallet.getCustomerId(),
				wallet.getAccountNumber(),
				wallet.getCurrency(),
				operationId,
				walletHold == null ? null : walletHold.getHoldId(),
				walletHold == null ? null : walletHold.getStatus(),
				amount,
				balance,
				purpose
		);
	}
}
