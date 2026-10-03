package com.vestra.common.event.payloads;

public record UserRegisteredPayload(
        String fullName,
        String email
) {}
