package com.vestra.payment.service.dto;

import com.vestra.payment.service.entity.Payment;
import com.vestra.payment.service.entity.PaymentMethod;
import com.vestra.payment.service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        String orderNumber,
        UUID userId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionId,
        String cardLastFour,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getCardLastFour(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
