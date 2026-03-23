package com.kd.wallet.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
public class LedgerEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, unique = true, length = 80)
	private String eventId;

	@Column(name = "source_topic", nullable = false, length = 80)
	private String sourceTopic;

	@Column(name = "event_type", nullable = false, length = 80)
	private String eventType;

	@Column(name = "aggregate_type", nullable = false, length = 40)
	private String aggregateType;

	@Column(name = "aggregate_key", nullable = false, length = 80)
	private String aggregateKey;

	@Column(name = "request_id", length = 80)
	private String requestId;

	@Column(name = "trace_id", length = 80)
	private String traceId;

	@Column(name = "reference_no", length = 80)
	private String referenceNo;

	@Column(name = "wallet_id")
	private Long walletId;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "customer_id", length = 40)
	private String customerId;

	@Column(name = "account_number", length = 20)
	private String accountNumber;

	@Column(name = "from_account_number", length = 20)
	private String fromAccountNumber;

	@Column(name = "to_account_number", length = 20)
	private String toAccountNumber;

	@Column(name = "currency", length = 10)
	private String currency;

	@Column(name = "operation_id", length = 80)
	private String operationId;

	@Column(name = "hold_id", length = 80)
	private String holdId;

	@Column(name = "status", length = 40)
	private String status;

	@Column(name = "amount", precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "balance", precision = 19, scale = 2)
	private BigDecimal balance;

	@Column(name = "purpose", length = 255)
	private String purpose;

	@Column(name = "error_code", length = 50)
	private String errorCode;

	@Column(name = "error_message", length = 255)
	private String errorMessage;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
