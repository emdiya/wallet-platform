package com.kd.wallet.ledger.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class LedgerKafkaConsumer {

	private final LedgerEventIngestionService ledgerEventIngestionService;

	public LedgerKafkaConsumer(LedgerEventIngestionService ledgerEventIngestionService) {
		this.ledgerEventIngestionService = ledgerEventIngestionService;
	}

	@KafkaListener(topics = "${ledger.kafka.wallet-topic:wallet.events}")
	public void consumeWalletEvent(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ledgerEventIngestionService.ingestWalletEvent(topic, payload);
	}

	@KafkaListener(topics = "${ledger.kafka.transfer-topic:transfer.events}")
	public void consumeTransferEvent(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ledgerEventIngestionService.ingestTransferEvent(topic, payload);
	}
}
