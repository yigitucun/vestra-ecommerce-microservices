package com.vestra.auth.service.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vestra.auth.service.dto.user.UserInfo;

public record AuthResponse(
        @JsonIgnore
        String accessToken,
        @JsonIgnore
        String refreshToken,
        @JsonProperty("user")
        UserInfo user
) {
}
