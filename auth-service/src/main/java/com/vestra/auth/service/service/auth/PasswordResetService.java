package com.vestra.auth.service.service.auth;

import com.vestra.auth.service.utils.TokenUtils;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final StringRedisTemplate redisTemplate;
    private static final String RESET_PASSWORD_PREFIX = "password_reset:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    public String createResetToken(String userId){
        String cooldownKey = RESET_PASSWORD_PREFIX + "cooldown:" + userId;
        Boolean exists = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(exists)){
            throw ApiException.badRequest("Bekle","Şifre sıfırlama bağlantısı için biraz bekleyin");
        }

        String token = TokenUtils.generateSecureToken();
        String tokenKey = RESET_PASSWORD_PREFIX+token;

        redisTemplate.opsForValue().set(tokenKey,userId,TOKEN_TTL);

        redisTemplate.opsForValue().set(cooldownKey,"1",Duration.ofMinutes(1));

        return token;
    }

    public String consumeToken(String token){
        String userId = redisTemplate.opsForValue().get(RESET_PASSWORD_PREFIX+token);
        if (userId!=null){
            redisTemplate.delete(RESET_PASSWORD_PREFIX+token);
        }
        return userId;
    }

}
