package com.vestra.common.event.payloads;

public record StockReservedPayload(
        String variantId,
        int reservedCount,
        int availableStock
) {
}
