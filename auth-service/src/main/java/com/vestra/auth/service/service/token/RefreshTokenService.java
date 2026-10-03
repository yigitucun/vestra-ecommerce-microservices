package com.vestra.auth.service.service.token;

import com.vestra.auth.service.utils.TokenUtils;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token-expiration-in}")
    private Long refreshTokenExpiration;
    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX= "user_tokens:";

    protected String create(String userId){
        String tokenId = TokenUtils.generateSecureToken();
        Duration ttl = Duration.ofDays(refreshTokenExpiration);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + tokenId,userId,ttl);
        redisTemplate.opsForSet().add(USER_TOKEN_PREFIX+userId,tokenId);
        redisTemplate.expire(USER_TOKEN_PREFIX+userId,ttl);
        return tokenId;
    }

    protected String validateAndGetUserId(String tokenId){
        String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX+tokenId);
        if (userId == null){
            throw ApiException.badRequest("Geçersiz Refresh token","Geçersiz veya süresi dolmuş.");
        }
        return userId;
    }

    protected void revoke(String tokenId, String userId){
        redisTemplate.delete(TOKEN_PREFIX+tokenId);
        redisTemplate.opsForSet().remove(USER_TOKEN_PREFIX+userId,tokenId);
    }

    protected String rotate(String oldTokenId){
        String userId = validateAndGetUserId(oldTokenId);
        revoke(oldTokenId,userId);
        return create(userId);
    }

    protected void revokeAll(String userId){
        Set<String>  tokenIds = redisTemplate.opsForSet().members(USER_TOKEN_PREFIX+userId);
        if (tokenIds != null){
            tokenIds.forEach(tokenId -> redisTemplate.delete(TOKEN_PREFIX+tokenId));
        }
        redisTemplate.delete(USER_TOKEN_PREFIX+userId);
    }


}
