package com.vestra.common.event.payloads;

import java.math.BigDecimal;

public record PaymentRefundedPayload(
        String paymentId,
        String orderId,
        String orderNumber,
        BigDecimal refundAmount,
        String reason
) {}
