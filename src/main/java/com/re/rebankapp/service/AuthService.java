package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);

    String register(RegisterRequest registerRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String accessToken);
}
