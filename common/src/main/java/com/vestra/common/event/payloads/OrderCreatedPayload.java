package com.vestra.common.event.payloads;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedPayload(
        String orderId,
        String orderNumber,
        String userId,
        String customerEmail,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            String variantId,
            String productName,
            String sku,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
