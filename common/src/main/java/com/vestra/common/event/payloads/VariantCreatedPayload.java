package com.vestra.common.event.payloads;

public record VariantCreatedPayload(
        String variantId,
        int initialStock
) { }
