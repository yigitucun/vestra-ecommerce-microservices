package com.vestra.common.event.payloads;

import java.math.BigDecimal;

public record PaymentCompletedPayload(
        String paymentId,
        String orderId,
        String orderNumber,
        String userId,
        BigDecimal amount,
        String paymentMethod,
        String transactionId
) {}
