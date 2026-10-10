package com.vestra.payment.service.service;

import com.vestra.common.dto.OutboxEventDTO;
import com.vestra.common.event.payloads.PaymentCompletedPayload;
import com.vestra.common.event.payloads.PaymentFailedPayload;
import com.vestra.common.event.payloads.PaymentRefundedPayload;
import com.vestra.common.event.types.PaymentEvents;
import com.vestra.payment.service.entity.OutboxEvent;
import com.vestra.payment.service.entity.Payment;
import com.vestra.payment.service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public void save(OutboxEventDTO event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(event.eventType())
                .aggregateType(event.aggregateType())
                .aggregateId(event.aggregateId())
                .payload(objectMapper.writeValueAsString(event.payload()))
                .build();
        eventRepository.save(outboxEvent);
    }

    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getOrderNumber(),
                payment.getUserId().toString(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getTransactionId()
        );

        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(PaymentEvents.PAYMENT_COMPLETED)
                .aggregateType("Payment")
                .aggregateId(payment.getId().toString())
                .payload(payload)
                .build();
        save(event);
    }

    public void publishPaymentFailedEvent(Payment payment) {
        PaymentFailedPayload payload = new PaymentFailedPayload(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getOrderNumber(),
                payment.getUserId().toString(),
                payment.getAmount(),
                payment.getFailureReason()
        );

        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(PaymentEvents.PAYMENT_FAILED)
                .aggregateType("Payment")
                .aggregateId(payment.getId().toString())
                .payload(payload)
                .build();
        save(event);
    }

    public void publishPaymentRefundedEvent(Payment payment, BigDecimal refundAmount, String reason) {
        PaymentRefundedPayload payload = new PaymentRefundedPayload(
                payment.getId().toString(),
                payment.getOrderId().toString(),
                payment.getOrderNumber(),
                refundAmount,
                reason
        );

        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(PaymentEvents.PAYMENT_REFUNDED)
                .aggregateType("Payment")
                .aggregateId(payment.getId().toString())
                .payload(payload)
                .build();
        save(event);
    }
}
