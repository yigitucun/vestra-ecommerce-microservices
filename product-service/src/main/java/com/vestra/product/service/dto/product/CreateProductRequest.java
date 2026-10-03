package com.vestra.product.service.dto.product;

import com.vestra.product.service.annotation.UniqueProductSlug;
import com.vestra.product.service.dto.variant.CreateVariantRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank String name,
        String description,
        String imageUrl,
        @NotNull UUID categoryId,
        @NotBlank @UniqueProductSlug String slug,
        List<@Valid CreateVariantRequest> variants
) { }
