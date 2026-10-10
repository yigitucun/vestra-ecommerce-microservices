package com.vestra.common.event.payloads;

import java.util.List;

public record OrderCancelledPayload(
        String orderId,
        String orderNumber,
        String reason,
        List<OrderItemReleasePayload> items
) {
    public record OrderItemReleasePayload(
            String variantId,
            int quantity
    ) {}
}
