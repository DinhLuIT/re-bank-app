package com.re.rebankapp.service.impl;

import com.re.rebankapp.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String token, long expireTimeInSeconds) {
        if (expireTimeInSeconds > 0) {
            redisTemplate.opsForValue().set(token, "blacklisted", expireTimeInSeconds, TimeUnit.SECONDS);
            log.info("Đã lưu token vào Redis với thời gian là: {}", expireTimeInSeconds);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
