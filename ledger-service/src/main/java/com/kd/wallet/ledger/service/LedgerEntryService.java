package com.kd.wallet.ledger.service;

import com.kd.wallet.ledger.dto.response.LedgerEntryResponse;

import java.util.List;

public interface LedgerEntryService {

	LedgerEntryResponse getById(Long id);

	List<LedgerEntryResponse> search(String requestId,
			String accountNumber,
			String eventType,
			String sourceTopic,
			String holdId,
			int limit);
}
