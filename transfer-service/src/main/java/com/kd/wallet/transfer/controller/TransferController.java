package com.kd.wallet.transfer.controller;

import com.kd.wallet.common.web.ApiResponse;
import com.kd.wallet.transfer.dto.request.CreateTransferRequest;
import com.kd.wallet.transfer.dto.response.TransferResponse;
import com.kd.wallet.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(Authentication authentication,
			@Valid @RequestBody CreateTransferRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(
						"Transfer created successfully",
						transferService.createTransfer(Long.valueOf(authentication.getName()), request)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TransferResponse>> getTransferById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Transfer fetched successfully", transferService.getTransferById(id)));
	}

	@GetMapping(params = "requestId")
	public ResponseEntity<ApiResponse<TransferResponse>> getTransferByRequestId(@RequestParam String requestId) {
		return ResponseEntity.ok(ApiResponse.success("Transfer fetched successfully", transferService.getTransferByRequestId(requestId)));
	}
}
