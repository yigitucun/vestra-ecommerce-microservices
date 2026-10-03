package com.vestra.auth.service.service.oauth2;

import com.vestra.auth.service.entity.User;
import com.vestra.auth.service.utils.TokenUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final StringRedisTemplate redisTemplate;
    @Value("${app.client-url}")
    private String clientUrl;
    private static final String EXCHANGE_PREFIX = "oauth2_exchange:";

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (oAuth2User == null) {
            response.sendRedirect(clientUrl + "/login?error=unauthorized");
            return;
        }

        User user = oAuth2User.getUser();

        String exchangeCode = TokenUtils.generateSecureToken();
        redisTemplate.opsForValue().set(
                EXCHANGE_PREFIX + exchangeCode,
                user.getId().toString(),
                Duration.ofSeconds(30)
        );

        response.sendRedirect(clientUrl + "/oauth2/redirect?code=" + exchangeCode);
    }
}
