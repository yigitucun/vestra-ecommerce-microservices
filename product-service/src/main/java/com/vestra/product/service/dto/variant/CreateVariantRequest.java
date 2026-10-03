package com.vestra.product.service.dto.variant;

import com.vestra.product.service.annotation.UniqueSKU;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateVariantRequest(
        @NotNull @Positive BigDecimal price,
        @NotBlank @UniqueSKU String sku,
        List<@Valid CreateVariantAttributesRequest> attributes,
        int initialStock
) { }
