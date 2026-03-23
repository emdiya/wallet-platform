package com.kd.wallet.transfer.service;

import com.kd.wallet.transfer.dto.request.CreateTransferRequest;
import com.kd.wallet.transfer.dto.response.TransferResponse;

public interface TransferService {

	TransferResponse createTransfer(Long authenticatedUserId, CreateTransferRequest request);

	TransferResponse getTransferById(Long id);

	TransferResponse getTransferByRequestId(String requestId);
}
