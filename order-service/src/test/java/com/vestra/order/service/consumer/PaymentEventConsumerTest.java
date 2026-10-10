package com.vestra.order.service.consumer;

import com.vestra.common.event.payloads.PaymentCompletedPayload;
import com.vestra.common.event.payloads.PaymentFailedPayload;
import com.vestra.common.event.payloads.PaymentRefundedPayload;
import com.vestra.common.event.types.PaymentEvents;
import com.vestra.order.service.entity.OrderStatus;
import com.vestra.order.service.service.OrderService;
import com.vestra.order.service.utils.OutboxMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OutboxMessageParser messageParser;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentEventConsumer consumer;

    @Test
    void shouldUpdateOrderStatusToPaidWhenPaymentCompleted() {
        UUID orderId = UUID.randomUUID();
        String json = "{}";
        byte[] header = PaymentEvents.PAYMENT_COMPLETED.getBytes(StandardCharsets.UTF_8);

        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                UUID.randomUUID().toString(),
                orderId.toString(),
                "ORD-123",
                UUID.randomUUID().toString(),
                BigDecimal.valueOf(1000),
                "CREDIT_CARD",
                "TXN-999"
        );

        when(messageParser.parse(json, PaymentCompletedPayload.class)).thenReturn(payload);

        consumer.consume(json, header);

        verify(orderService).updateOrderStatus(orderId, OrderStatus.PAID);
    }

    @Test
    void shouldCancelOrderWhenPaymentFailed() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String json = "{}";
        byte[] header = PaymentEvents.PAYMENT_FAILED.getBytes(StandardCharsets.UTF_8);

        PaymentFailedPayload payload = new PaymentFailedPayload(
                UUID.randomUUID().toString(),
                orderId.toString(),
                "ORD-123",
                userId.toString(),
                BigDecimal.valueOf(1000),
                "Yetersiz bakiye"
        );

        when(messageParser.parse(json, PaymentFailedPayload.class)).thenReturn(payload);

        consumer.consume(json, header);

        verify(orderService).cancelOrder(eq(orderId), eq(userId), contains("Yetersiz bakiye"), eq(true));
    }

    @Test
    void shouldUpdateOrderStatusToCancelledWhenPaymentRefunded() {
        UUID orderId = UUID.randomUUID();
        String json = "{}";
        byte[] header = PaymentEvents.PAYMENT_REFUNDED.getBytes(StandardCharsets.UTF_8);

        PaymentRefundedPayload payload = new PaymentRefundedPayload(
                UUID.randomUUID().toString(),
                orderId.toString(),
                "ORD-123",
                BigDecimal.valueOf(1000),
                "İptal talebi"
        );

        when(messageParser.parse(json, PaymentRefundedPayload.class)).thenReturn(payload);

        consumer.consume(json, header);

        verify(orderService).updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }
}
