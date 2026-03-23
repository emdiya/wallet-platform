package com.kd.wallet.ledger.controller;

import com.kd.wallet.common.web.ApiResponse;
import com.kd.wallet.ledger.dto.response.LedgerEntryResponse;
import com.kd.wallet.ledger.service.LedgerEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

	private final LedgerEntryService ledgerEntryService;

	public LedgerController(LedgerEntryService ledgerEntryService) {
		this.ledgerEntryService = ledgerEntryService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<LedgerEntryResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Ledger entry fetched successfully", ledgerEntryService.getById(id)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> search(
			@RequestParam(required = false) String requestId,
			@RequestParam(required = false) String accountNumber,
			@RequestParam(required = false) String eventType,
			@RequestParam(required = false) String sourceTopic,
			@RequestParam(required = false) String holdId,
			@RequestParam(defaultValue = "100") int limit) {
		return ResponseEntity.ok(ApiResponse.success(
				"Ledger entries fetched successfully",
				ledgerEntryService.search(requestId, accountNumber, eventType, sourceTopic, holdId, limit)));
	}
}
