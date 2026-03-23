package com.kd.wallet.auth.service.impl;

import com.kd.wallet.auth.dto.request.LoginRequest;
import com.kd.wallet.auth.dto.request.RegisterRequest;
import com.kd.wallet.auth.dto.request.SetupTpinRequest;
import com.kd.wallet.auth.dto.request.VerifyTpinRequest;
import com.kd.wallet.auth.dto.response.LoginResponse;
import com.kd.wallet.auth.dto.response.UserResponse;
import com.kd.wallet.auth.entity.User;
import com.kd.wallet.auth.exception.DuplicateResourceException;
import com.kd.wallet.auth.exception.InvalidCredentialsException;
import com.kd.wallet.auth.exception.ResourceNotFoundException;
import com.kd.wallet.auth.logging.LoggerClient;
import com.kd.wallet.auth.mapper.UserMapper;
import com.kd.wallet.auth.repository.UserRepository;
import com.kd.wallet.auth.security.JwtService;
import com.kd.wallet.auth.service.AuthService;
import com.kd.wallet.auth.service.WalletProvisioningClient;
import com.kd.wallet.auth.util.BankingIdentityUtils;
import com.kd.wallet.auth.util.PasswordUtils;
import com.kd.wallet.auth.util.PhoneUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final LoggerClient loggerClient;
	private final WalletProvisioningClient walletProvisioningClient;

	public AuthServiceImpl(UserRepository userRepository,
			UserMapper userMapper,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			LoggerClient loggerClient,
			WalletProvisioningClient walletProvisioningClient) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.loggerClient = loggerClient;
		this.walletProvisioningClient = walletProvisioningClient;
	}

	@Override
	@Transactional
	public UserResponse register(RegisterRequest request) {
		String normalizedPhone = PhoneUtils.normalize(request.phone());
		PasswordUtils.validateStrength(request.password());

		if (userRepository.existsByPhone(normalizedPhone)) {
			loggerClient.warn("Registration rejected", "phone=" + normalizedPhone + ", reason=Duplicate phone registration attempt");
			throw new DuplicateResourceException("User already exists with phone: " + normalizedPhone);
		}

		User user = userMapper.toEntity(request, normalizedPhone, passwordEncoder.encode(request.password()));
		user.setCustomerId(BankingIdentityUtils.createCustomerId());
		user.setAccountNumber(BankingIdentityUtils.createAccountNumber());
		user.setAccountName(user.getFullName());
		User savedUser = userRepository.saveAndFlush(user);
		walletProvisioningClient.createWallet(
				savedUser.getId(),
				savedUser.getCustomerId(),
				savedUser.getAccountName(),
				savedUser.getAccountNumber());
		loggerClient.info("User registered", "phone=" + normalizedPhone + ", userId=" + savedUser.getId());
		return userMapper.toUserResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String normalizedPhone = PhoneUtils.normalize(request.phone());
		User user = userRepository.findByPhone(normalizedPhone)
				.orElseThrow(() -> {
					loggerClient.warn("Login rejected", "phone=" + normalizedPhone + ", reason=Unknown phone");
					return new InvalidCredentialsException("Invalid phone or password");
				});

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			loggerClient.warn("Login rejected", "phone=" + normalizedPhone + ", reason=Password mismatch");
			throw new InvalidCredentialsException("Invalid phone or password");
		}

		String token = jwtService.generateToken(user);
		LocalDateTime expiresAt = LocalDateTime.ofInstant(jwtService.extractExpiration(token), ZoneOffset.UTC);
		loggerClient.info("User authenticated", "phone=" + normalizedPhone + ", userId=" + user.getId());
		return userMapper.toLoginResponse(user, token, expiresAt);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		return userMapper.toUserResponse(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserByPhone(String phone) {
		String normalizedPhone = PhoneUtils.normalize(phone);
		User user = userRepository.findByPhone(normalizedPhone)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + normalizedPhone));
		return userMapper.toUserResponse(user);
	}

	@Override
	@Transactional
	public void setupTpin(Long userId, SetupTpinRequest request) {
		if (!request.tpin().equals(request.confirmTpin())) {
			throw new IllegalArgumentException("TPIN and confirm TPIN do not match");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		user.setTpinHash(passwordEncoder.encode(request.tpin()));
		userRepository.save(user);
		loggerClient.info("TPIN configured", "userId=" + userId);
	}

	@Override
	@Transactional(readOnly = true)
	public void verifyTpin(Long userId, VerifyTpinRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		if (user.getTpinHash() == null || user.getTpinHash().isBlank()) {
			throw new InvalidCredentialsException("TPIN is not set for this account");
		}
		if (!passwordEncoder.matches(request.tpin(), user.getTpinHash())) {
			loggerClient.warn("TPIN verification failed", "userId=" + userId + ", reason=TPIN mismatch");
			throw new InvalidCredentialsException("Invalid TPIN");
		}
	}

}
