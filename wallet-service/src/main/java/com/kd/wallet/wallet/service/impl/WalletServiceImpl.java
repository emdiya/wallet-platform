package com.kd.wallet.wallet.service.impl;

import com.kd.wallet.wallet.dto.request.CommitHoldRequest;
import com.kd.wallet.wallet.dto.request.CreateMyWalletRequest;
import com.kd.wallet.wallet.dto.request.CreateWalletRequest;
import com.kd.wallet.wallet.dto.request.ReleaseHoldRequest;
import com.kd.wallet.wallet.dto.request.ReserveHoldRequest;
import com.kd.wallet.wallet.dto.request.TopUpMyWalletRequest;
import com.kd.wallet.wallet.dto.request.TopUpWalletRequest;
import com.kd.wallet.wallet.dto.response.WalletHoldResponse;
import com.kd.wallet.wallet.dto.response.WalletResponse;
import com.kd.wallet.wallet.entity.Wallet;
import com.kd.wallet.wallet.entity.WalletHold;
import com.kd.wallet.wallet.entity.WalletOperation;
import com.kd.wallet.wallet.exception.DuplicateResourceException;
import com.kd.wallet.wallet.exception.InsufficientBalanceException;
import com.kd.wallet.wallet.exception.ResourceNotFoundException;
import com.kd.wallet.wallet.logging.LoggerClient;
import com.kd.wallet.wallet.mapper.WalletMapper;
import com.kd.wallet.wallet.repository.WalletHoldRepository;
import com.kd.wallet.wallet.repository.WalletOperationRepository;
import com.kd.wallet.wallet.repository.WalletRepository;
import com.kd.wallet.wallet.service.WalletEventPublisher;
import com.kd.wallet.wallet.service.WalletService;
import com.kd.wallet.wallet.service.TpinVerificationClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WalletServiceImpl implements WalletService {

	private static final String HOLD_STATUS_RESERVED = "RESERVED";
	private static final String HOLD_STATUS_COMMITTED = "COMMITTED";
	private static final String HOLD_STATUS_RELEASED = "RELEASED";

	private final WalletRepository walletRepository;
	private final WalletHoldRepository walletHoldRepository;
	private final WalletOperationRepository walletOperationRepository;
	private final WalletMapper walletMapper;
	private final WalletEventPublisher walletEventPublisher;
	private final TpinVerificationClient tpinVerificationClient;
	private final LoggerClient loggerClient;

	public WalletServiceImpl(WalletRepository walletRepository,
			WalletHoldRepository walletHoldRepository,
			WalletOperationRepository walletOperationRepository,
			WalletMapper walletMapper,
			WalletEventPublisher walletEventPublisher,
			TpinVerificationClient tpinVerificationClient,
			LoggerClient loggerClient) {
		this.walletRepository = walletRepository;
		this.walletHoldRepository = walletHoldRepository;
		this.walletOperationRepository = walletOperationRepository;
		this.walletMapper = walletMapper;
		this.walletEventPublisher = walletEventPublisher;
		this.tpinVerificationClient = tpinVerificationClient;
		this.loggerClient = loggerClient;
	}

	@Override
	@Transactional
	public WalletResponse createWallet(CreateWalletRequest request) {
		Long userId = requireUserId(request.userId());
		String customerId = requireText(request.customerId(), "Customer id is required");
		String accountName = requireText(request.accountName(), "Account name is required");
		String normalizedCurrency = normalizeCurrency(request.currency());
		if (walletRepository.existsByUserIdAndCurrency(userId, normalizedCurrency)) {
			throw new DuplicateResourceException(
					"Wallet already exists for userId: " + userId + " and currency: " + normalizedCurrency);
		}
		Wallet wallet = walletMapper.toEntity(request);
		wallet.setUserId(userId);
		wallet.setCustomerId(customerId);
		wallet.setAccountName(accountName);
		wallet.setCurrency(normalizedCurrency);
		if (wallet.getAccountNumber() == null || wallet.getAccountNumber().isBlank()) {
			wallet.setAccountNumber(generateUniqueAccountNumber());
		} else if (walletRepository.existsByAccountNumber(wallet.getAccountNumber())) {
			throw new DuplicateResourceException("Wallet already exists for accountNumber: " + wallet.getAccountNumber());
		}
		Wallet savedWallet = walletRepository.save(wallet);
		walletEventPublisher.publishWalletCreated(savedWallet);
		loggerClient.info("Wallet created",
				"userId=" + savedWallet.getUserId() + ", walletId=" + savedWallet.getId()
						+ ", accountNumber=" + savedWallet.getAccountNumber());
		return walletMapper.toResponse(savedWallet);
	}

	@Override
	@Transactional
	public WalletResponse createWalletForAuthenticatedUser(Long userId, CreateMyWalletRequest request) {
		Wallet primaryWallet = walletRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Primary wallet not found for userId: " + userId));

		CreateWalletRequest createWalletRequest = new CreateWalletRequest(
				primaryWallet.getUserId(),
				primaryWallet.getCustomerId(),
				primaryWallet.getAccountName(),
				null,
				request.currency()
		);

		return createWallet(createWalletRequest);
	}

	@Override
	@Transactional(readOnly = true)
	public WalletResponse getWalletById(Long id) {
		return walletMapper.toResponse(findWalletById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public WalletResponse getWalletByUserId(Long userId) {
		return walletMapper.toResponse(walletRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with userId: " + userId)));
	}

	@Override
	@Transactional(readOnly = true)
	public WalletResponse getWalletByCustomerId(String customerId) {
		String normalizedCustomerId = requireText(customerId, "Customer id is required");
		return walletMapper.toResponse(walletRepository.findFirstByCustomerIdOrderByCreatedAtAsc(normalizedCustomerId)
				.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with customerId: " + normalizedCustomerId)));
	}

	@Override
	@Transactional(readOnly = true)
	public WalletResponse getWalletByAccountNumber(String accountNumber) {
		return walletMapper.toResponse(findWalletByAccountNumber(accountNumber));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WalletResponse> getWalletsByUserId(Long userId) {
		return walletRepository.findByUserIdOrderByCreatedAtAsc(userId)
				.stream()
				.map(walletMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public WalletResponse topUp(TopUpWalletRequest request) {
		Wallet wallet = findWalletByAccountNumber(request.accountNumber());
		String operationId = normalizeOperationId(request.operationId());
		if (walletOperationRepository.existsByOperationId(operationId)) {
			return walletMapper.toResponse(wallet);
		}

		BigDecimal amount = normalizeAmount(request.amount());
		wallet.setBalance(wallet.getBalance().add(amount));
		Wallet savedWallet = walletRepository.save(wallet);
		saveOperation(operationId, "TOP_UP");
		walletEventPublisher.publishWalletTopUp(savedWallet, operationId, normalizeNullable(request.purpose()));
		loggerClient.info("Wallet topped up",
				"walletId=" + savedWallet.getId() + ", operationId=" + operationId + ", amount=" + amount);
		return walletMapper.toResponse(savedWallet);
	}

	@Override
	@Transactional
	public WalletResponse topUpForAuthenticatedUser(Long userId, TopUpMyWalletRequest request) {
		Wallet wallet = findWalletByAccountNumber(request.accountNumber());
		if (!wallet.getUserId().equals(userId)) {
			throw new IllegalArgumentException("You can only top up your own wallet");
		}
		tpinVerificationClient.verify(request.tpin());
		return topUp(new TopUpWalletRequest(
				request.accountNumber(),
				request.operationId(),
				request.amount(),
				request.purpose()
		));
	}

	@Override
	@Transactional
	public WalletHoldResponse reserveHold(ReserveHoldRequest request) {
		Wallet wallet = findWalletByAccountNumber(request.accountNumber());
		String operationId = normalizeOperationId(request.operationId());
		if (walletOperationRepository.existsByOperationId(operationId)) {
			return walletMapper.toResponse(findHoldById(request.holdId()));
		}
		if (walletHoldRepository.findByHoldId(requireText(request.holdId(), "Hold id is required")).isPresent()) {
			throw new DuplicateResourceException("Hold already exists with holdId: " + request.holdId().trim());
		}

		BigDecimal amount = normalizeAmount(request.amount());
		if (wallet.getBalance().compareTo(amount) < 0) {
			throw new InsufficientBalanceException("Insufficient wallet balance for hold reserve");
		}

		wallet.setBalance(wallet.getBalance().subtract(amount));
		Wallet savedWallet = walletRepository.save(wallet);

		WalletHold walletHold = new WalletHold();
		walletHold.setHoldId(request.holdId().trim());
		walletHold.setOperationId(operationId);
		walletHold.setWalletId(savedWallet.getId());
		walletHold.setUserId(savedWallet.getUserId());
		walletHold.setAmount(amount);
		walletHold.setStatus(HOLD_STATUS_RESERVED);
		walletHold.setPurpose(normalizeNullable(request.purpose()));

		WalletHold savedHold = walletHoldRepository.save(walletHold);
		saveOperation(operationId, "HOLD_RESERVE");
		walletEventPublisher.publishHoldReserved(savedWallet, savedHold);
		loggerClient.info("Wallet hold reserved",
				"walletId=" + savedWallet.getId() + ", holdId=" + savedHold.getHoldId() + ", amount=" + amount);
		return walletMapper.toResponse(savedHold);
	}

	@Override
	@Transactional
	public WalletHoldResponse commitHold(CommitHoldRequest request) {
		String operationId = normalizeOperationId(request.operationId());
		WalletHold walletHold = findHoldById(request.holdId());
		if (walletOperationRepository.existsByOperationId(operationId)) {
			return walletMapper.toResponse(walletHold);
		}
		if (!HOLD_STATUS_RESERVED.equals(walletHold.getStatus())) {
			throw new IllegalArgumentException("Only RESERVED holds can be committed");
		}

		walletHold.setStatus(HOLD_STATUS_COMMITTED);
		walletHold.setProcessedAt(LocalDateTime.now());
		if (request.purpose() != null && !request.purpose().isBlank()) {
			walletHold.setPurpose(request.purpose().trim());
		}
		WalletHold savedHold = walletHoldRepository.save(walletHold);
		saveOperation(operationId, "HOLD_COMMIT");
		Wallet wallet = findWalletById(savedHold.getWalletId());
		walletEventPublisher.publishHoldCommitted(wallet, savedHold, operationId, savedHold.getPurpose());
		loggerClient.info("Wallet hold committed",
				"walletId=" + wallet.getId() + ", holdId=" + savedHold.getHoldId() + ", operationId=" + operationId);
		return walletMapper.toResponse(savedHold);
	}

	@Override
	@Transactional
	public WalletHoldResponse releaseHold(ReleaseHoldRequest request) {
		String operationId = normalizeOperationId(request.operationId());
		WalletHold walletHold = findHoldById(request.holdId());
		if (walletOperationRepository.existsByOperationId(operationId)) {
			return walletMapper.toResponse(walletHold);
		}
		if (!HOLD_STATUS_RESERVED.equals(walletHold.getStatus())) {
			throw new IllegalArgumentException("Only RESERVED holds can be released");
		}

		Wallet wallet = findWalletById(walletHold.getWalletId());
		wallet.setBalance(wallet.getBalance().add(walletHold.getAmount()));
		Wallet savedWallet = walletRepository.save(wallet);

		walletHold.setStatus(HOLD_STATUS_RELEASED);
		walletHold.setProcessedAt(LocalDateTime.now());
		if (request.purpose() != null && !request.purpose().isBlank()) {
			walletHold.setPurpose(request.purpose().trim());
		}
		WalletHold savedHold = walletHoldRepository.save(walletHold);
		saveOperation(operationId, "HOLD_RELEASE");
		walletEventPublisher.publishHoldReleased(savedWallet, savedHold, operationId, savedHold.getPurpose());
		loggerClient.info("Wallet hold released",
				"walletId=" + savedWallet.getId() + ", holdId=" + savedHold.getHoldId() + ", operationId=" + operationId);
		return walletMapper.toResponse(savedHold);
	}

	private Wallet findWalletById(Long id) {
		return walletRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
	}

	private Wallet findWalletByAccountNumber(String accountNumber) {
		String normalizedAccountNumber = requireText(accountNumber, "Account number is required");
		return walletRepository.findByAccountNumber(normalizedAccountNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with accountNumber: " + normalizedAccountNumber));
	}

	private WalletHold findHoldById(String holdId) {
		String normalizedHoldId = requireText(holdId, "Hold id is required");
		return walletHoldRepository.findByHoldId(normalizedHoldId)
				.orElseThrow(() -> new ResourceNotFoundException("Wallet hold not found with holdId: " + normalizedHoldId));
	}

	private BigDecimal normalizeAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}
		return amount;
	}

	private String normalizeOperationId(String operationId) {
		return requireText(operationId, "Operation id is required");
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}

	private Long requireUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User id is required");
		}
		return userId;
	}

	private String normalizeNullable(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String normalizeCurrency(String currency) {
		if (currency == null || currency.isBlank()) {
			return "USD";
		}
		String normalized = currency.trim().toUpperCase(Locale.ROOT);
		if (!normalized.equals("USD") && !normalized.equals("KHR")) {
			throw new IllegalArgumentException("Currency must be USD or KHR");
		}
		return normalized;
	}

	private String generateUniqueAccountNumber() {
		String accountNumber;
		do {
			accountNumber = "85502" + randomDigits(10);
		} while (walletRepository.existsByAccountNumber(accountNumber));
		return accountNumber;
	}

	private String randomDigits(int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int index = 0; index < length; index++) {
			builder.append(ThreadLocalRandom.current().nextInt(10));
		}
		return builder.toString();
	}

	private void saveOperation(String operationId, String opType) {
		WalletOperation walletOperation = new WalletOperation();
		walletOperation.setOperationId(operationId);
		walletOperation.setOpType(opType);
		walletOperationRepository.save(walletOperation);
	}
}
