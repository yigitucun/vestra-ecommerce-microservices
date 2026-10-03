package com.vestra.auth.service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private static final String ATTEMPTS_PREFIX = "login_attempts:";
    @Value("${security.login.max-attempts}")
    private int maxAttempts;
    @Value("${security.login.lockout-minutes}")
    private long lockoutMinutes;

    public boolean isLocked(String email){
        String attempts = redisTemplate.opsForValue().get(ATTEMPTS_PREFIX+email);
        return attempts !=null && Integer.parseInt(attempts) >= maxAttempts;
    }

    public void recordFailedAttempt(String email){
        String key = ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1){
            redisTemplate.expire(key, Duration.ofMinutes(lockoutMinutes));
        }
    }

    public void resetAttempts(String email){
        redisTemplate.delete(ATTEMPTS_PREFIX+email);
    }

}
