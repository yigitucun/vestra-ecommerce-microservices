package com.vestra.common.event.payloads;

public record StockReservationFailedPayload(
        String orderId,
        String orderNumber,
        String reason
) {
}
