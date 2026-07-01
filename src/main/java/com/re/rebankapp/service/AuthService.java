package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.request.VerifyOtpRequest;
import com.re.rebankapp.dto.request.ResetPasswordRequest;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.dto.response.ResetTokenResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);

    String register(RegisterRequest registerRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String accessToken);

    void forgotPassword(String email);

    ResetTokenResponse verifyOtp(VerifyOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
