package com.kd.wallet.transfer.service;

import com.kd.wallet.transfer.dto.response.WalletHoldResponse;
import com.kd.wallet.transfer.dto.response.WalletResponse;

import java.math.BigDecimal;

public interface WalletClient {

	WalletResponse getByAccountNumber(String accountNumber);

	WalletHoldResponse reserveHold(String accountNumber, String holdId, String operationId, BigDecimal amount, String purpose);

	WalletHoldResponse commitHold(String holdId, String operationId, String purpose);

	WalletHoldResponse releaseHold(String holdId, String operationId, String purpose);

	WalletResponse topUp(String accountNumber, String operationId, BigDecimal amount, String purpose);
}
