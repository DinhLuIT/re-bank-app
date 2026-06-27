package com.re.rebankapp.service.impl;

import com.re.rebankapp.entity.RefreshToken;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.RefreshTokenRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    @Override
    public RefreshToken createRefreshToken(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        // tìm token cũ ( nếu có ), nếu không có thì tạo token mới
        RefreshToken refreshToken = refreshTokenRepository.findByToken(user.getUsername())
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token){
        if (token.getExpiryDate().compareTo(Instant.now()) < 0 || token.getRevoked()){
            refreshTokenRepository.delete(token);
            throw new AppException(ResponseCode.TOKEN_EXPIRED);
        }

        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
}
