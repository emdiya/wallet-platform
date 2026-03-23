package com.kd.wallet.ledger.mapper;

import com.kd.wallet.ledger.dto.response.LedgerEntryResponse;
import com.kd.wallet.ledger.entity.LedgerEntry;

public final class LedgerEntryMapper {

	private LedgerEntryMapper() {
	}

	public static LedgerEntryResponse toResponse(LedgerEntry entry) {
		return new LedgerEntryResponse(
				entry.getId(),
				entry.getEventId(),
				entry.getSourceTopic(),
				entry.getEventType(),
				entry.getAggregateType(),
				entry.getAggregateKey(),
				entry.getRequestId(),
				entry.getTraceId(),
				entry.getReferenceNo(),
				entry.getWalletId(),
				entry.getUserId(),
				entry.getCustomerId(),
				entry.getAccountNumber(),
				entry.getFromAccountNumber(),
				entry.getToAccountNumber(),
				entry.getCurrency(),
				entry.getOperationId(),
				entry.getHoldId(),
				entry.getStatus(),
				entry.getAmount(),
				entry.getBalance(),
				entry.getPurpose(),
				entry.getErrorCode(),
				entry.getErrorMessage(),
				entry.getOccurredAt(),
				entry.getCreatedAt(),
				entry.getPayload()
		);
	}
}
