package com.kd.wallet.wallet.controller;

import com.kd.wallet.common.web.ApiResponse;
import com.kd.wallet.wallet.dto.request.CommitHoldRequest;
import com.kd.wallet.wallet.dto.request.CreateMyWalletRequest;
import com.kd.wallet.wallet.dto.request.CreateWalletRequest;
import com.kd.wallet.wallet.dto.request.ReleaseHoldRequest;
import com.kd.wallet.wallet.dto.request.ReserveHoldRequest;
import com.kd.wallet.wallet.dto.request.TopUpMyWalletRequest;
import com.kd.wallet.wallet.dto.request.TopUpWalletRequest;
import com.kd.wallet.wallet.dto.response.WalletHoldResponse;
import com.kd.wallet.wallet.dto.response.WalletResponse;
import com.kd.wallet.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<WalletResponse>> createWallet(Authentication authentication,
			@Valid @RequestBody CreateWalletRequest request) {
		WalletResponse response;
		if (authentication != null && request.userId() == null) {
			response = walletService.createWalletForAuthenticatedUser(
					Long.valueOf(authentication.getName()),
					new CreateMyWalletRequest(request.currency()));
		} else {
			response = walletService.createWallet(request);
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Wallet created successfully", response));
	}

	@PostMapping("/me")
	public ResponseEntity<ApiResponse<WalletResponse>> createMyWallet(Authentication authentication,
			@Valid @RequestBody CreateMyWalletRequest request) {
		Long userId = Long.valueOf(authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(
						"Wallet created successfully",
						walletService.createWalletForAuthenticatedUser(userId, request)));
	}

	@GetMapping("/{id:\\d+}")
	public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Wallet fetched successfully", walletService.getWalletById(id)));
	}

	@GetMapping(params = "userId")
	public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(@RequestParam Long userId) {
		return ResponseEntity.ok(ApiResponse.success("Wallet fetched successfully", walletService.getWalletByUserId(userId)));
	}

	@GetMapping("/by-customer")
	public ResponseEntity<ApiResponse<WalletResponse>> getWalletByCustomerId(@RequestParam String customerId) {
		return ResponseEntity.ok(ApiResponse.success("Wallet fetched successfully", walletService.getWalletByCustomerId(customerId)));
	}

	@GetMapping("/by-account")
	public ResponseEntity<ApiResponse<WalletResponse>> getWalletByAccountNumber(@RequestParam String accountNumber) {
		return ResponseEntity.ok(ApiResponse.success("Wallet fetched successfully", walletService.getWalletByAccountNumber(accountNumber)));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<WalletResponse>>> getMyWallets(Authentication authentication) {
		Long userId = Long.valueOf(authentication.getName());
		return ResponseEntity.ok(ApiResponse.success("Wallets fetched successfully", walletService.getWalletsByUserId(userId)));
	}

	@PostMapping("/top-ups")
	public ResponseEntity<ApiResponse<WalletResponse>> topUp(@Valid @RequestBody TopUpWalletRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Wallet topped up successfully", walletService.topUp(request)));
	}

	@PostMapping("/me/top-ups")
	public ResponseEntity<ApiResponse<WalletResponse>> topUpMyWallet(Authentication authentication,
			@Valid @RequestBody TopUpMyWalletRequest request) {
		Long userId = Long.valueOf(authentication.getName());
		return ResponseEntity.ok(ApiResponse.success(
				"Wallet topped up successfully",
				walletService.topUpForAuthenticatedUser(userId, request)));
	}

	@PostMapping("/holds/reserve")
	public ResponseEntity<ApiResponse<WalletHoldResponse>> reserveHold(@Valid @RequestBody ReserveHoldRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Wallet hold reserved successfully", walletService.reserveHold(request)));
	}

	@PostMapping("/holds/commit")
	public ResponseEntity<ApiResponse<WalletHoldResponse>> commitHold(@Valid @RequestBody CommitHoldRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Wallet hold committed successfully", walletService.commitHold(request)));
	}

	@PostMapping("/holds/release")
	public ResponseEntity<ApiResponse<WalletHoldResponse>> releaseHold(@Valid @RequestBody ReleaseHoldRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Wallet hold released successfully", walletService.releaseHold(request)));
	}
}
