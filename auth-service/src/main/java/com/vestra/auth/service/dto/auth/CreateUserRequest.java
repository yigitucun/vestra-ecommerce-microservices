package com.vestra.auth.service.dto.auth;

import com.vestra.auth.service.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @UniqueEmail
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        @NotBlank(message = "E-posta adresi boş bırakılamaz")
        String email,

        @NotBlank(message = "Ad alanı boş bırakılamaz")
        String firstName,

        @NotBlank(message = "Soyad alanı boş bırakılamaz")
        String lastName,

        @NotBlank(message = "Şifre boş bırakılamaz")
        @Size(min = 6, max = 100, message = "Şifre en az 6 karakter olmalıdır")
        String password
) { }
