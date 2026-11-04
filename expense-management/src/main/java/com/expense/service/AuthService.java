
package com.expense.service;

import com.expense.dto.request.LoginRequest;
import com.expense.dto.request.RefreshTokenRequest;
import com.expense.dto.request.RegisterRequest;
import com.expense.dto.response.AuthResponse;
import com.expense.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
