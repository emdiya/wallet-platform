package com.kd.wallet.wallet.mapper;

import com.kd.wallet.wallet.dto.request.CreateWalletRequest;
import com.kd.wallet.wallet.dto.response.WalletHoldResponse;
import com.kd.wallet.wallet.dto.response.WalletResponse;
import com.kd.wallet.wallet.entity.Wallet;
import com.kd.wallet.wallet.entity.WalletHold;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class WalletMapper {

	public Wallet toEntity(CreateWalletRequest request) {
		Wallet wallet = new Wallet();
		wallet.setUserId(request.userId());
		wallet.setCustomerId(request.customerId().trim());
		wallet.setAccountName(request.accountName().trim());
		if (request.accountNumber() != null && !request.accountNumber().isBlank()) {
			wallet.setAccountNumber(request.accountNumber().trim());
		}
		wallet.setCurrency(normalizeCurrency(request.currency()));
		return wallet;
	}

	public WalletResponse toResponse(Wallet wallet) {
		return new WalletResponse(
				wallet.getId(),
				wallet.getUserId(),
				wallet.getCustomerId(),
				wallet.getAccountName(),
				wallet.getAccountNumber(),
				wallet.getCurrency(),
				wallet.getBalance(),
				wallet.getCreatedAt()
		);
	}

	public WalletHoldResponse toResponse(WalletHold walletHold) {
		return new WalletHoldResponse(
				walletHold.getId(),
				walletHold.getHoldId(),
				walletHold.getOperationId(),
				walletHold.getWalletId(),
				walletHold.getUserId(),
				walletHold.getAmount(),
				walletHold.getStatus(),
				walletHold.getPurpose(),
				walletHold.getCreatedAt(),
				walletHold.getProcessedAt()
		);
	}

	private String normalizeCurrency(String currency) {
		if (currency == null || currency.isBlank()) {
			return "USD";
		}
		return currency.trim().toUpperCase(Locale.ROOT);
	}
}
