package com.kd.wallet.transfer.service.impl;

import com.kd.wallet.transfer.dto.request.CreateTransferRequest;
import com.kd.wallet.transfer.dto.response.TransferResponse;
import com.kd.wallet.transfer.dto.response.WalletResponse;
import com.kd.wallet.transfer.entity.Transfer;
import com.kd.wallet.transfer.exception.ResourceNotFoundException;
import com.kd.wallet.transfer.exception.TransferFailedException;
import com.kd.wallet.transfer.logging.LoggerClient;
import com.kd.wallet.transfer.mapper.TransferMapper;
import com.kd.wallet.transfer.repository.TransferRepository;
import com.kd.wallet.transfer.service.TransferEventPublisher;
import com.kd.wallet.transfer.service.TransferService;
import com.kd.wallet.transfer.service.TpinVerificationClient;
import com.kd.wallet.transfer.service.WalletClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {

	private final TransferRepository transferRepository;
	private final TransferMapper transferMapper;
	private final WalletClient walletClient;
	private final TransferEventPublisher transferEventPublisher;
	private final TpinVerificationClient tpinVerificationClient;
	private final LoggerClient loggerClient;

	public TransferServiceImpl(TransferRepository transferRepository,
			TransferMapper transferMapper,
			WalletClient walletClient,
			TransferEventPublisher transferEventPublisher,
			TpinVerificationClient tpinVerificationClient,
			LoggerClient loggerClient) {
		this.transferRepository = transferRepository;
		this.transferMapper = transferMapper;
		this.walletClient = walletClient;
		this.transferEventPublisher = transferEventPublisher;
		this.tpinVerificationClient = tpinVerificationClient;
		this.loggerClient = loggerClient;
	}

	@Override
	@Transactional
	public TransferResponse createTransfer(Long authenticatedUserId, CreateTransferRequest request) {
		String requestId = requireText(request.requestId(), "Request id is required");
		if (transferRepository.findByRequestId(requestId).isPresent()) {
			return transferMapper.toResponse(transferRepository.findByRequestId(requestId).orElseThrow());
		}

		String fromAccountNumber = requireText(request.fromAccountNumber(), "From account number is required");
		String toAccountNumber = requireText(request.toAccountNumber(), "To account number is required");
		if (fromAccountNumber.equals(toAccountNumber)) {
			throw new IllegalArgumentException("Source and destination accounts must be different");
		}

		BigDecimal amount = normalizeAmount(request.amount());
		WalletResponse sourceWallet = walletClient.getByAccountNumber(fromAccountNumber);
		WalletResponse destinationWallet = walletClient.getByAccountNumber(toAccountNumber);
		if (!sourceWallet.userId().equals(authenticatedUserId)) {
			throw new IllegalArgumentException("You can only transfer from your own wallet");
		}
		tpinVerificationClient.verify(request.tpin());

		String holdId = "hold-" + requestId;
		String referenceNo = "TRX-" + requestId;
		Transfer transfer = transferMapper.toEntity(request, referenceNo, holdId);
		transfer.setFromUserId(sourceWallet.userId());
		transfer.setToUserId(destinationWallet.userId());
		Transfer savedTransfer = transferRepository.save(transfer);
		transferEventPublisher.publishStarted(savedTransfer);

		try {
			walletClient.reserveHold(fromAccountNumber, holdId, requestId + "-reserve", amount, normalizeNullable(request.purpose()));
			walletClient.topUp(toAccountNumber, requestId + "-credit", amount, normalizeNullable(request.purpose()));
			walletClient.commitHold(holdId, requestId + "-commit", normalizeNullable(request.purpose()));

			savedTransfer.setStatus("COMPLETED");
			savedTransfer.setCompletedAt(LocalDateTime.now());
			savedTransfer.setErrorCode(null);
			savedTransfer.setErrorMessage(null);
			Transfer completedTransfer = transferRepository.save(savedTransfer);
			transferEventPublisher.publishCompleted(completedTransfer);
			loggerClient.info("Transfer completed",
					"requestId=" + requestId + ", referenceNo=" + referenceNo + ", amount=" + amount);
			return transferMapper.toResponse(completedTransfer);
		} catch (Exception exception) {
			try {
				walletClient.releaseHold(holdId, requestId + "-release", "transfer rollback");
			} catch (Exception rollbackException) {
				loggerClient.warn("Transfer rollback failed",
						"requestId=" + requestId + ", holdId=" + holdId + ", reason=" + rollbackException.getMessage());
			}

			savedTransfer.setStatus("FAILED");
			savedTransfer.setCompletedAt(LocalDateTime.now());
			savedTransfer.setErrorCode("TRANSFER_FAILED");
			savedTransfer.setErrorMessage(exception.getMessage());
			Transfer failedTransfer = transferRepository.save(savedTransfer);
			transferEventPublisher.publishFailed(failedTransfer);
			loggerClient.error("Transfer failed",
					"requestId=" + requestId + ", referenceNo=" + referenceNo + ", reason=" + exception.getMessage());
			throw new TransferFailedException("Transfer failed: " + exception.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResponse getTransferById(Long id) {
		return transferMapper.toResponse(transferRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + id)));
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResponse getTransferByRequestId(String requestId) {
		String normalizedRequestId = requireText(requestId, "Request id is required");
		return transferMapper.toResponse(transferRepository.findByRequestId(normalizedRequestId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with requestId: " + normalizedRequestId)));
	}

	private BigDecimal normalizeAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}
		return amount;
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}

	private String normalizeNullable(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
