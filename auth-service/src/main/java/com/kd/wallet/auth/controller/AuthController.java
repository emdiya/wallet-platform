package com.kd.wallet.auth.controller;

import com.kd.wallet.auth.dto.request.LoginRequest;
import com.kd.wallet.auth.dto.request.RegisterRequest;
import com.kd.wallet.auth.dto.request.SetupTpinRequest;
import com.kd.wallet.auth.dto.request.VerifyTpinRequest;
import com.kd.wallet.auth.dto.response.LoginResponse;
import com.kd.wallet.auth.dto.response.UserResponse;
import com.kd.wallet.auth.service.AuthService;
import com.kd.wallet.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("User registered successfully", response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
		UserResponse response = authService.getUserById(id);
		return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
	}

	@GetMapping("/users/by-phone")
	public ResponseEntity<ApiResponse<UserResponse>> getUserByPhone(@RequestParam String phone) {
		UserResponse response = authService.getUserByPhone(phone);
		return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
	}

	@PostMapping("/tpin/setup")
	public ResponseEntity<ApiResponse<Void>> setupTpin(Authentication authentication,
			@Valid @RequestBody SetupTpinRequest request) {
		authService.setupTpin(Long.valueOf(authentication.getName()), request);
		return ResponseEntity.ok(ApiResponse.success("TPIN set successfully", null));
	}

	@PostMapping("/tpin/verify")
	public ResponseEntity<ApiResponse<Void>> verifyTpin(Authentication authentication,
			@Valid @RequestBody VerifyTpinRequest request) {
		authService.verifyTpin(Long.valueOf(authentication.getName()), request);
		return ResponseEntity.ok(ApiResponse.success("TPIN verified successfully", null));
	}

}
