package com.re.rebankapp.service;

public interface TokenBlacklistService {
    void blacklistToken(String token, long expireTimeInSeconds);
    boolean isTokenBlacklisted(String token);
}
