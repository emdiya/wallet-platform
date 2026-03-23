package com.kd.wallet.transfer.service;

import com.kd.wallet.transfer.entity.Transfer;
import com.kd.wallet.transfer.event.TransferEvent;

public interface TransferEventPublisher {

	void publishStarted(Transfer transfer);

	void publishCompleted(Transfer transfer);

	void publishFailed(Transfer transfer);

	void publish(String key, TransferEvent event);
}
