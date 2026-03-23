package com.kd.wallet.auth.service;

public interface WalletProvisioningClient {

	void createWallet(Long userId, String customerId, String accountName, String accountNumber);
}
