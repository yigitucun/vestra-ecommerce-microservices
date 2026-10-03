package com.vestra.auth.service.dto.user;

import com.vestra.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank(message = "Ad alanı boş olamaz")
        String firstName,

        @NotBlank(message = "Soyad alanı boş olamaz")
        String lastName,

        @NotBlank(message = "E-posta alanı boş olamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String email,

        @NotNull(message = "Rol alanı zorunludur")
        Role role,

        @NotNull(message = "Aktiflik durumu zorunludur")
        Boolean isActive,

        String password
) {}
