package com.vestra.product.service.dto.product;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        BigDecimal price,
        String sku,
        Map<String, String> attributes,
        Integer initialStock
) {}