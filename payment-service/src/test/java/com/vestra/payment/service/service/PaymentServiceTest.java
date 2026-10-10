package com.vestra.payment.service.service;

import com.vestra.common.web.exception.ApiException;
import com.vestra.payment.service.dto.PaymentResponse;
import com.vestra.payment.service.dto.ProcessPaymentRequest;
import com.vestra.payment.service.dto.RefundPaymentRequest;
import com.vestra.payment.service.entity.Payment;
import com.vestra.payment.service.entity.PaymentMethod;
import com.vestra.payment.service.entity.PaymentStatus;
import com.vestra.payment.service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldProcessPaymentSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-12345",
                BigDecimal.valueOf(1500),
                PaymentMethod.CREDIT_CARD,
                "5432543254325432",
                "Ahmet Yılmaz",
                "12",
                "28",
                "123"
        );

        when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaymentResponse response = paymentService.processPayment(userId, request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals("5432", response.cardLastFour());
        assertNotNull(response.transactionId());
        assertEquals(BigDecimal.valueOf(1500), response.amount());

        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventService).publishPaymentCompletedEvent(any());
    }

    @Test
    void shouldFailAndPublishFailedEventWhenCardDeclined() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        // Son 4 hanesi "0000" olan kart simülasyonda reddedilir
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-99999",
                BigDecimal.valueOf(500),
                PaymentMethod.CREDIT_CARD,
                "4000000000000000",
                "Mehmet Demir",
                "01",
                "27",
                "999"
        );

        when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        assertThrows(ApiException.class, () -> paymentService.processPayment(userId, request));

        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED));
        verify(outboxEventService).publishPaymentFailedEvent(any());
        verify(outboxEventService, never()).publishPaymentCompletedEvent(any());
    }

    @Test
    void shouldThrowBadRequestWhenOrderAlreadyPaid() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-12345",
                BigDecimal.valueOf(1500),
                PaymentMethod.CREDIT_CARD,
                "5432543254325432",
                "Ahmet Yılmaz",
                "12",
                "28",
                "123"
        );

        Payment existingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(existingPayment));

        assertThrows(ApiException.class, () -> paymentService.processPayment(userId, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldRefundPaymentWhenSuccess() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(paymentId)
                .orderNumber("ORD-123")
                .amount(BigDecimal.valueOf(1000))
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundPaymentRequest request = new RefundPaymentRequest("Müşteri talebi");
        PaymentResponse response = paymentService.refundPayment(paymentId, request);

        assertEquals(PaymentStatus.REFUNDED, response.status());
        verify(outboxEventService).publishPaymentRefundedEvent(eq(payment), eq(BigDecimal.valueOf(1000)), eq("Müşteri talebi"));
    }

    @Test
    void shouldThrowBadRequestWhenRefundingNonSuccessPayment() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(paymentId)
                .status(PaymentStatus.FAILED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        RefundPaymentRequest request = new RefundPaymentRequest("Hatalı işlem");
        assertThrows(ApiException.class, () -> paymentService.refundPayment(paymentId, request));
        verify(paymentRepository, never()).save(any());
    }
}
