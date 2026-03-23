package com.kd.wallet.ledger.service.impl;

import com.kd.wallet.ledger.dto.response.LedgerEntryResponse;
import com.kd.wallet.ledger.entity.LedgerEntry;
import com.kd.wallet.ledger.exception.ResourceNotFoundException;
import com.kd.wallet.ledger.mapper.LedgerEntryMapper;
import com.kd.wallet.ledger.repository.LedgerEntryRepository;
import com.kd.wallet.ledger.service.LedgerEntryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LedgerEntryServiceImpl implements LedgerEntryService {

	private final LedgerEntryRepository ledgerEntryRepository;

	public LedgerEntryServiceImpl(LedgerEntryRepository ledgerEntryRepository) {
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Override
	public LedgerEntryResponse getById(Long id) {
		LedgerEntry entry = ledgerEntryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Ledger entry not found"));
		return LedgerEntryMapper.toResponse(entry);
	}

	@Override
	public List<LedgerEntryResponse> search(String requestId,
			String accountNumber,
			String eventType,
			String sourceTopic,
			String holdId,
			int limit) {
		if (limit < 1 || limit > 500) {
			throw new IllegalArgumentException("Limit must be between 1 and 500");
		}
		return ledgerEntryRepository.search(
						requestId,
						accountNumber,
						eventType,
						sourceTopic,
						holdId,
						PageRequest.of(0, limit))
				.stream()
				.map(LedgerEntryMapper::toResponse)
				.toList();
	}
}
