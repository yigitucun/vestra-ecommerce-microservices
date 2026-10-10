package com.vestra.product.service.dto.variant;

import java.math.BigDecimal;
import java.util.UUID;

public record VariantInfoResponse(
        UUID variantId,
        UUID productId,
        String productName,
        String sku,
        BigDecimal price
) {}
