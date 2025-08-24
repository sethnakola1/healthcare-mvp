package com.healthcare.mvp.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtBlocklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLOCKLIST_PREFIX = "jti_blocklist:";

    public void blocklist(String jti, Date expiration) {
        long remainingValidity = expiration.getTime() - System.currentTimeMillis();
        if (remainingValidity > 0) {
            redisTemplate.opsForValue().set(BLOCKLIST_PREFIX + jti, "", remainingValidity, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlocklisted(String jti) {
        return redisTemplate.hasKey(BLOCKLIST_PREFIX + jti);
    }
}
