package com.vestra.payment.service.service;

import com.vestra.common.web.exception.ApiException;
import com.vestra.payment.service.client.OrderServiceClient;
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

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldProcessPaymentSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-12345",
                userId,
                BigDecimal.valueOf(1500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

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
    void shouldRejectPaymentWhenOrderDoesNotBelongToUser_IDOR() {
        UUID currentUserId = UUID.randomUUID();
        UUID realOwnerUserId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-12345",
                realOwnerUserId,
                BigDecimal.valueOf(1500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

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

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.processPayment(currentUserId, request));
        assertEquals("Yetkisiz Erişim", ex.getTitle());
        verify(paymentRepository, never()).save(any());
        verify(outboxEventService, never()).publishPaymentCompletedEvent(any());
    }

    @Test
    void shouldRejectPaymentWhenAmountDoesNotMatchOrder_AmountTampering() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-12345",
                userId,
                BigDecimal.valueOf(1500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

        // Saldırgan 1500 TL sipariş için 1 TL ödeme yapmaya çalışıyor
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-12345",
                BigDecimal.valueOf(1),
                PaymentMethod.CREDIT_CARD,
                "5432543254325432",
                "Ahmet Yılmaz",
                "12",
                "28",
                "123"
        );

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.processPayment(userId, request));
        assertEquals("Tutar Uyuşmazlığı", ex.getTitle());
        verify(paymentRepository, never()).save(any());
        verify(outboxEventService, never()).publishPaymentCompletedEvent(any());
    }

    @Test
    void shouldRejectPaymentWhenOrderStatusIsNotPending() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-12345",
                userId,
                BigDecimal.valueOf(1500),
                "CANCELLED"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

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

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.processPayment(userId, request));
        assertEquals("Geçersiz Sipariş Durumu", ex.getTitle());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldFailAndPublishFailedEventWhenCardDeclined() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-99999",
                userId,
                BigDecimal.valueOf(500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

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

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-12345",
                userId,
                BigDecimal.valueOf(1500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

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

    @Test
    void shouldProcessPaymentSuccessfullyWithPaymentToken() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-99999",
                userId,
                BigDecimal.valueOf(2500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-99999",
                BigDecimal.valueOf(2500),
                PaymentMethod.CREDIT_CARD,
                "tok_visa_success_1234",
                "1234"
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
        assertEquals("1234", response.cardLastFour());
        assertNotNull(response.transactionId());
        assertEquals(BigDecimal.valueOf(2500), response.amount());

        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventService).publishPaymentCompletedEvent(any());
    }

    @Test
    void shouldDeclinePaymentWhenTokenIsDeclined() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderServiceClient.OrderDetails orderDetails = new OrderServiceClient.OrderDetails(
                orderId,
                "ORD-99999",
                userId,
                BigDecimal.valueOf(2500),
                "PENDING"
        );
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderDetails);

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                orderId,
                "ORD-99999",
                BigDecimal.valueOf(2500),
                PaymentMethod.CREDIT_CARD,
                "tok_declined_card",
                "0000"
        );

        when(paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ApiException exception = assertThrows(ApiException.class, () -> paymentService.processPayment(userId, request));
        assertEquals("Ödeme Reddedildi", exception.getTitle());

        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventService).publishPaymentFailedEvent(any());
    }

    @Test
    void shouldMaskSensitiveDataInToString() {
        UUID fixedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                fixedId,
                "ORD-12345",
                BigDecimal.valueOf(500),
                PaymentMethod.CREDIT_CARD,
                "4111222233334444",
                "Secret Name",
                "11",
                "30",
                "999"
        );

        String result = request.toString();
        assertFalse(result.contains("4111222233334444"), "Ham kart numarası log/toString çıktısında yer almamalıdır");
        assertFalse(result.contains("999"), "CVV log/toString çıktısında asla yer almamalıdır");
        assertFalse(result.contains("Secret Name"), "Kart sahibi ismi log/toString çıktısında yer almamalıdır");
        assertTrue(result.contains("4444"), "Maskeli son 4 hane görünmelidir");
        assertTrue(result.contains("cvv=***"), "CVV maskelenmiş olmalıdır");
    }

    @Test
    void shouldValidatePaymentInputProperly() {
        ProcessPaymentRequest tokenRequest = new ProcessPaymentRequest(
                UUID.randomUUID(),
                "ORD-1",
                BigDecimal.valueOf(100),
                PaymentMethod.CREDIT_CARD,
                "tok_123",
                "1234"
        );
        assertTrue(tokenRequest.isValidPaymentInput());

        ProcessPaymentRequest invalidRequest = new ProcessPaymentRequest(
                UUID.randomUUID(),
                "ORD-2",
                BigDecimal.valueOf(100),
                PaymentMethod.CREDIT_CARD,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertFalse(invalidRequest.isValidPaymentInput());
    }
}
