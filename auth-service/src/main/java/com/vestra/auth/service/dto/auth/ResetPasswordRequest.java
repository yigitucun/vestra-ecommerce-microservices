package com.vestra.auth.service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6,message = "Minimum 6 karakter olmalıdır.") String newPassword
) {
}
