package com.vestra.auth.service.controller;

import com.vestra.auth.service.dto.auth.AuthResponse;
import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.repository.UserRepository;
import com.vestra.auth.service.service.token.TokenService;
import com.vestra.auth.service.utils.CookieUtils;
import com.vestra.common.web.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class Oauth2Controller {

    @GetMapping("/oauth2/authorization/{provider}")
    public void redirectToApiAuthAuthorization(
            @PathVariable String provider,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String queryString = request.getQueryString();
        String target = "/api/auth/oauth2/authorization/" + provider + (queryString != null && !queryString.isBlank() ? "?" + queryString : "");
        response.sendRedirect(target);
    }

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final CookieUtils cookieUtils;

    @PostMapping("/api/auth/oauth2/exchange")
    public ResponseEntity<AuthResponse> exchangeCode(@RequestParam String code){
        String key = "oauth2_exchange:"+code;
        String userId = redisTemplate.opsForValue().getAndDelete(key);

        if (userId == null) {
            throw ApiException.badRequest("Geçersiz ya da süresi dolmuş kod", "Geçersiz ya da süresi dolmuş kod");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> ApiException.badRequest("Kullanıcı bulunamadı", null));

        AuthResponse response = tokenService.create(user);

        HttpHeaders headers = new HttpHeaders();
        cookieUtils.addAuthCookies(headers,response);
        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

}
