package com.vestra.auth.service.dto.user;

import java.util.UUID;

public record UserInfo(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String role,
    String createdAt,
    boolean active
) { }
