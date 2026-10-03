package com.vestra.auth.service.controller;

import com.vestra.auth.service.dto.auth.*;
import com.vestra.auth.service.service.auth.AuthService;
import com.vestra.auth.service.utils.CookieUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request){
        AuthResponse response = authService.register(request);
        HttpHeaders headers = new HttpHeaders();
        cookieUtils.addAuthCookies(headers,response);
        return ResponseEntity.status(201)
                .headers(headers)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        AuthResponse response = this.authService.login(request);
        HttpHeaders headers = new HttpHeaders();
        cookieUtils.addAuthCookies(headers,response);
        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token",required = false) String refreshToken
    ) {
        AuthResponse response = authService.refresh(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        cookieUtils.addAuthCookies(headers,response);
        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token",required = false) String refreshToken
    ){
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookieUtils.buildLogoutCookie(CookieUtils.ACCESS_TOKEN_NAME).toString())
                .header(HttpHeaders.SET_COOKIE,cookieUtils.buildLogoutCookie(CookieUtils.REFRESH_TOKEN_NAME).toString())
                .header(HttpHeaders.SET_COOKIE,cookieUtils.buildLogoutCookie(CookieUtils.AUTH_FLAG_NAME).toString())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal Jwt jwt){
        String userId = jwt.getSubject();
        authService.logoutAll(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

}
