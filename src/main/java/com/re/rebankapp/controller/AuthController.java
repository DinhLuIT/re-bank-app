package com.re.rebankapp.controller;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String registerResult = authService.register(registerRequest);

        return ResponseEntity.ok(ApiResponse.success(registerResult));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String accessToken = headerAuth.substring(7);
            authService.logout(accessToken);
            return ResponseEntity.ok(ApiResponse.success());
        }
        
        throw new AppException(ResponseCode.UNAUTHORIZED);
    }
}
