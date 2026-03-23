package com.kd.wallet.auth.service;

import com.kd.wallet.auth.dto.request.LoginRequest;
import com.kd.wallet.auth.dto.request.RegisterRequest;
import com.kd.wallet.auth.dto.request.SetupTpinRequest;
import com.kd.wallet.auth.dto.request.VerifyTpinRequest;
import com.kd.wallet.auth.dto.response.LoginResponse;
import com.kd.wallet.auth.dto.response.UserResponse;

public interface AuthService {

	UserResponse register(RegisterRequest request);

	LoginResponse login(LoginRequest request);

	UserResponse getUserById(Long id);

	UserResponse getUserByPhone(String phone);

	void setupTpin(Long userId, SetupTpinRequest request);

	void verifyTpin(Long userId, VerifyTpinRequest request);

}
