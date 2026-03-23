package com.kd.wallet.transfer.service.impl;

import com.kd.wallet.common.logging.RequestMetadata;
import com.kd.wallet.common.logging.RequestMetadataContext;
import com.kd.wallet.transfer.entity.Transfer;
import com.kd.wallet.transfer.event.TransferEvent;
import com.kd.wallet.transfer.logging.LoggerClient;
import com.kd.wallet.transfer.service.TransferEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaTransferEventPublisher implements TransferEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaTransferEventPublisher.class);

	private final KafkaTemplate<String, TransferEvent> kafkaTemplate;
	private final LoggerClient loggerClient;
	private final String topicName;

	public KafkaTransferEventPublisher(KafkaTemplate<String, TransferEvent> kafkaTemplate,
			LoggerClient loggerClient,
			@Value("${transfer.kafka.topic:transfer.events}") String topicName) {
		this.kafkaTemplate = kafkaTemplate;
		this.loggerClient = loggerClient;
		this.topicName = topicName;
	}

	@Override
	public void publishStarted(Transfer transfer) {
		publish(transfer.getRequestId(), buildEvent("TransferStarted", transfer));
	}

	@Override
	public void publishCompleted(Transfer transfer) {
		publish(transfer.getRequestId(), buildEvent("TransferCompleted", transfer));
	}

	@Override
	public void publishFailed(Transfer transfer) {
		publish(transfer.getRequestId(), buildEvent("TransferFailed", transfer));
	}

	@Override
	public void publish(String key, TransferEvent event) {
		try {
			kafkaTemplate.send(topicName, key, event);
		} catch (Exception exception) {
			log.error("Kafka publish failed for topic={} key={} eventType={}", topicName, key, event.eventType(), exception);
			loggerClient.warn("Transfer event publish failed",
					"topic=" + topicName + ", key=" + key + ", eventType=" + event.eventType()
							+ ", reason=" + exception.getMessage());
		}
	}

	private TransferEvent buildEvent(String eventType, Transfer transfer) {
		RequestMetadata metadata = RequestMetadataContext.get();
		return new TransferEvent(
				UUID.randomUUID().toString(),
				eventType,
				Instant.now(),
				metadata == null ? null : metadata.traceId(),
				transfer.getRequestId(),
				transfer.getReferenceNo(),
				transfer.getFromAccountNumber(),
				transfer.getToAccountNumber(),
				transfer.getAmount(),
				transfer.getStatus(),
				transfer.getHoldId(),
				transfer.getErrorCode(),
				transfer.getErrorMessage(),
				transfer.getPurpose()
		);
	}
}
