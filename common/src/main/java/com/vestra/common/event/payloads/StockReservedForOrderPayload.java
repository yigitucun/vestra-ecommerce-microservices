package com.vestra.common.event.payloads;

public record StockReservedForOrderPayload(
        String orderId,
        String orderNumber
) {
}
