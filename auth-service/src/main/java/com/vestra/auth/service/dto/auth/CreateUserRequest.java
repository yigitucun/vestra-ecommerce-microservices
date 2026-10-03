package com.vestra.auth.service.dto.auth;

import com.vestra.auth.service.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateUserRequest(
        @UniqueEmail @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Length(min = 6,message = "Minimum 6 karakter olmalıdır.") String password
) {
}
