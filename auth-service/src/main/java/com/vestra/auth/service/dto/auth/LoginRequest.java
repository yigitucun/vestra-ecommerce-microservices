package com.vestra.auth.service.dto.auth;

public record LoginRequest(
        String email,
        String password
) { }
