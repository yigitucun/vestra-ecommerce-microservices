package com.vestra.product.service.dto.product;

import com.vestra.product.service.dto.variant.UpdateVariantRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank(message = "Ürün adı boş olamaz")
        String name,

        @NotBlank(message = "Slug boş olamaz")
        String slug,

        String description,

        String imageUrl,

        @NotNull(message = "Kategori seçilmelidir")
        UUID categoryId,

        @NotEmpty(message = "En az bir varyant olmalıdır")
        List<@Valid UpdateVariantRequest> variants
) {}
