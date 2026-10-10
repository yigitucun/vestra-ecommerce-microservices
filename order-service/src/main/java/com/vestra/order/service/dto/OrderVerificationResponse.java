package com.vestra.order.service.dto;

import com.vestra.order.service.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderVerificationResponse(
        UUID orderId,
        String orderNumber,
        UUID userId,
        BigDecimal totalAmount,
        OrderStatus status
) {}
