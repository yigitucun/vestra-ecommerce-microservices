package com.vestra.auth.service.utils;

import com.vestra.auth.service.dto.auth.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public final class CookieUtils {

    @Value("${jwt.refresh-token-expiration-in}")
    private long refreshTokenExpiration;
    @Value("${jwt.access-token-expiration-in}")
    private long accessTokenExpiration;
    @Value("${cookie.secure}")
    private boolean isSecure;
    @Value("${cookie.same-site}")
    private String sameSite;
    private static final String PATH = "/";
    public static final String REFRESH_TOKEN_NAME = "refresh_token";
    public static final String ACCESS_TOKEN_NAME = "access_token";
    public static final String AUTH_FLAG_NAME = "is_authenticated";

    public void addAuthCookies(HttpHeaders headers,AuthResponse response){
        headers.add(HttpHeaders.SET_COOKIE,buildRefreshTokenCookie(response.refreshToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE,buildAccessTokenCookie(response.accessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE,buildAuthFlagCookie().toString());
    }

    public ResponseCookie buildLogoutCookie(String cookieName){
        return buildBaseCookie(cookieName,"",Duration.ZERO);
    }

    private ResponseCookie buildAccessTokenCookie(String accessToken){
        return buildBaseCookie(ACCESS_TOKEN_NAME,accessToken,Duration.ofMinutes(accessTokenExpiration));
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken){
        return buildBaseCookie(REFRESH_TOKEN_NAME,refreshToken,Duration.ofDays(refreshTokenExpiration));
    }

    private ResponseCookie buildAuthFlagCookie(){
        return ResponseCookie.from(AUTH_FLAG_NAME, "true")
                .httpOnly(false)
                .secure(isSecure)
                .path(PATH)
                .sameSite(sameSite)
                .maxAge(Duration.ofDays(refreshTokenExpiration))
                .build();
    }

    private ResponseCookie buildBaseCookie(String name,String value,Duration maxAge){
        return ResponseCookie.from(name,value)
                .httpOnly(true)
                .secure(isSecure)
                .path(PATH)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
    }




}
