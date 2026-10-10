package com.vestra.order.service.dto;

import com.vestra.order.service.entity.Order;
import com.vestra.order.service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    public record OrderItemResponse(
            UUID id,
            UUID variantId,
            String productName,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getVariantId(),
                        item.getProductName(),
                        item.getSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
