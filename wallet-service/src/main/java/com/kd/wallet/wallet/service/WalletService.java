package com.kd.wallet.wallet.service;

import com.kd.wallet.wallet.dto.request.CommitHoldRequest;
import com.kd.wallet.wallet.dto.request.CreateMyWalletRequest;
import com.kd.wallet.wallet.dto.request.CreateWalletRequest;
import com.kd.wallet.wallet.dto.request.ReleaseHoldRequest;
import com.kd.wallet.wallet.dto.request.ReserveHoldRequest;
import com.kd.wallet.wallet.dto.request.TopUpMyWalletRequest;
import com.kd.wallet.wallet.dto.request.TopUpWalletRequest;
import com.kd.wallet.wallet.dto.response.WalletHoldResponse;
import com.kd.wallet.wallet.dto.response.WalletResponse;

import java.util.List;

public interface WalletService {

	WalletResponse createWallet(CreateWalletRequest request);

	WalletResponse createWalletForAuthenticatedUser(Long userId, CreateMyWalletRequest request);

	WalletResponse getWalletById(Long id);

	WalletResponse getWalletByUserId(Long userId);

	WalletResponse getWalletByCustomerId(String customerId);

	WalletResponse getWalletByAccountNumber(String accountNumber);

	List<WalletResponse> getWalletsByUserId(Long userId);

	WalletResponse topUp(TopUpWalletRequest request);

	WalletResponse topUpForAuthenticatedUser(Long userId, TopUpMyWalletRequest request);

	WalletHoldResponse reserveHold(ReserveHoldRequest request);

	WalletHoldResponse commitHold(CommitHoldRequest request);

	WalletHoldResponse releaseHold(ReleaseHoldRequest request);
}
