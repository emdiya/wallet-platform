package com.kd.wallet.ledger.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.wallet.ledger.entity.LedgerEntry;
import com.kd.wallet.ledger.event.TransferEvent;
import com.kd.wallet.ledger.event.WalletEvent;
import com.kd.wallet.ledger.logging.LoggerClient;
import com.kd.wallet.ledger.repository.LedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerEventIngestionService {

	private static final Logger log = LoggerFactory.getLogger(LedgerEventIngestionService.class);

	private final ObjectMapper objectMapper;
	private final LedgerEntryRepository ledgerEntryRepository;
	private final LoggerClient loggerClient;

	public LedgerEventIngestionService(ObjectMapper objectMapper,
			LedgerEntryRepository ledgerEntryRepository,
			LoggerClient loggerClient) {
		this.objectMapper = objectMapper;
		this.ledgerEntryRepository = ledgerEntryRepository;
		this.loggerClient = loggerClient;
	}

	@Transactional
	public void ingestWalletEvent(String topic, String payload) {
		WalletEvent event = readValue(payload, WalletEvent.class);
		if (ledgerEntryRepository.existsByEventId(event.eventId())) {
			log.info("Skipping duplicate wallet event {}", event.eventId());
			return;
		}

		LedgerEntry entry = new LedgerEntry();
		entry.setEventId(event.eventId());
		entry.setSourceTopic(topic);
		entry.setEventType(event.eventType());
		entry.setAggregateType("WALLET");
		entry.setAggregateKey(event.accountNumber());
		entry.setRequestId(event.requestId());
		entry.setTraceId(event.traceId());
		entry.setWalletId(event.walletId());
		entry.setUserId(event.userId());
		entry.setCustomerId(event.customerId());
		entry.setAccountNumber(event.accountNumber());
		entry.setCurrency(event.currency());
		entry.setOperationId(event.operationId());
		entry.setHoldId(event.holdId());
		entry.setStatus(event.status());
		entry.setAmount(event.amount());
		entry.setBalance(event.balance());
		entry.setPurpose(event.purpose());
		entry.setOccurredAt(event.occurredAt());
		entry.setPayload(payload);
		ledgerEntryRepository.save(entry);
		loggerClient.info("Ledger entry stored",
				"topic=" + topic + ", eventId=" + event.eventId() + ", eventType=" + event.eventType());
	}

	@Transactional
	public void ingestTransferEvent(String topic, String payload) {
		TransferEvent event = readValue(payload, TransferEvent.class);
		if (ledgerEntryRepository.existsByEventId(event.eventId())) {
			log.info("Skipping duplicate transfer event {}", event.eventId());
			return;
		}

		LedgerEntry entry = new LedgerEntry();
		entry.setEventId(event.eventId());
		entry.setSourceTopic(topic);
		entry.setEventType(event.eventType());
		entry.setAggregateType("TRANSFER");
		entry.setAggregateKey(event.requestId());
		entry.setRequestId(event.requestId());
		entry.setTraceId(event.traceId());
		entry.setReferenceNo(event.referenceNo());
		entry.setFromAccountNumber(event.fromAccountNumber());
		entry.setToAccountNumber(event.toAccountNumber());
		entry.setAmount(event.amount());
		entry.setStatus(event.status());
		entry.setHoldId(event.holdId());
		entry.setPurpose(event.purpose());
		entry.setErrorCode(event.errorCode());
		entry.setErrorMessage(event.errorMessage());
		entry.setOccurredAt(event.occurredAt());
		entry.setPayload(payload);
		ledgerEntryRepository.save(entry);
		loggerClient.info("Ledger entry stored",
				"topic=" + topic + ", eventId=" + event.eventId() + ", eventType=" + event.eventType());
	}

	private <T> T readValue(String payload, Class<T> type) {
		try {
			return objectMapper.readValue(payload, type);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Failed to parse ledger event payload", exception);
		}
	}
}
