package com.vestra.common.event.payloads;

public record ResetPasswordPayload(
        String email,
        String token
) { }
