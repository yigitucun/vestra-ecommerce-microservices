package com.vestra.common.event.payloads;

public record StockUpdatedPayload(
        String variantId,
        int availableStock,
        int quantity,
        int reserved
) {
}
