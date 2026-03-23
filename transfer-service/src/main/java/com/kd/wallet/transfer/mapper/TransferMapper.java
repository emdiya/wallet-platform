package com.kd.wallet.transfer.mapper;

import com.kd.wallet.transfer.dto.request.CreateTransferRequest;
import com.kd.wallet.transfer.dto.response.TransferResponse;
import com.kd.wallet.transfer.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

	public Transfer toEntity(CreateTransferRequest request, String referenceNo, String holdId) {
		Transfer transfer = new Transfer();
		transfer.setRequestId(request.requestId().trim());
		transfer.setFromAccountNumber(request.fromAccountNumber().trim());
		transfer.setToAccountNumber(request.toAccountNumber().trim());
		transfer.setAmount(request.amount());
		transfer.setPurpose(normalizeNullable(request.purpose()));
		transfer.setReferenceNo(referenceNo);
		transfer.setHoldId(holdId);
		transfer.setStatus("PROCESSING");
		return transfer;
	}

	public TransferResponse toResponse(Transfer transfer) {
		return new TransferResponse(
				transfer.getId(),
				transfer.getRequestId(),
				transfer.getReferenceNo(),
				transfer.getFromAccountNumber(),
				transfer.getToAccountNumber(),
				transfer.getAmount(),
				transfer.getStatus(),
				transfer.getHoldId(),
				transfer.getPurpose(),
				transfer.getErrorCode(),
				transfer.getErrorMessage(),
				transfer.getCreatedAt(),
				transfer.getCompletedAt()
		);
	}

	private String normalizeNullable(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
