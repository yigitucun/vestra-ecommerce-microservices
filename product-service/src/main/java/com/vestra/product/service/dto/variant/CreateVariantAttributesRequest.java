package com.vestra.product.service.dto.variant;

import jakarta.validation.constraints.NotBlank;

public record CreateVariantAttributesRequest(
        @NotBlank String attributeName,
        @NotBlank String attributeValue
) { }
