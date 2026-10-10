package com.vestra.common.event.payloads;

import java.math.BigDecimal;

public record PaymentFailedPayload(
        String paymentId,
        String orderId,
        String orderNumber,
        String userId,
        BigDecimal amount,
        String failureReason
) {}
