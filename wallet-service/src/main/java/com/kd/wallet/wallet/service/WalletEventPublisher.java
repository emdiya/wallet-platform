package com.kd.wallet.wallet.service;

import com.kd.wallet.wallet.entity.Wallet;
import com.kd.wallet.wallet.entity.WalletHold;
import com.kd.wallet.wallet.event.WalletEvent;

public interface WalletEventPublisher {

	void publishWalletCreated(Wallet wallet);

	void publishWalletTopUp(Wallet wallet, String operationId, String purpose);

	void publishHoldReserved(Wallet wallet, WalletHold walletHold);

	void publishHoldCommitted(Wallet wallet, WalletHold walletHold, String operationId, String purpose);

	void publishHoldReleased(Wallet wallet, WalletHold walletHold, String operationId, String purpose);

	void publish(String key, WalletEvent event);
}
