package com.vestra.product.service.dto.variant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateVariantRequest(
        UUID id,

        @NotBlank(message = "SKU boş olamaz")
        String sku,

        @NotNull(message = "Fiyat boş olamaz")
        @Positive(message = "Fiyat 0'dan büyük olmalıdır")
        BigDecimal price,

        Integer initialStock,

        List<@Valid CreateVariantAttributesRequest> attributes
) {}
