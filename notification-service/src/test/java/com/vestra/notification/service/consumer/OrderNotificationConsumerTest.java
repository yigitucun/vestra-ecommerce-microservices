package com.vestra.notification.service.consumer;

import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.event.types.OrderEvents;
import com.vestra.notification.service.service.notifier.OrderConfirmationNotifier;
import com.vestra.notification.service.utils.OutboxMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderNotificationConsumerTest {

    @Mock
    private OutboxMessageParser messageParser;

    @Mock
    private OrderConfirmationNotifier notifier;

    @InjectMocks
    private OrderNotificationConsumer consumer;

    @Test
    void shouldConsumeOrderCreatedEventAndNotify() {
        String json = "{\"orderId\":\"123\"}";
        byte[] eventTypeHeader = OrderEvents.ORDER_CREATED.getBytes(StandardCharsets.UTF_8);

        OrderCreatedPayload payload = new OrderCreatedPayload(
                "order-123",
                "ORD-999",
                "user-456",
                "musteri@vestra.com",
                BigDecimal.valueOf(2500),
                "Kadıköy, İstanbul",
                List.of()
        );

        when(messageParser.parse(json, OrderCreatedPayload.class)).thenReturn(payload);

        consumer.consume(json, eventTypeHeader);

        verify(messageParser).parse(json, OrderCreatedPayload.class);
        verify(notifier).notify(payload);
    }

    @Test
    void shouldIgnoreWhenEventTypeHeaderMissing() {
        consumer.consume("{}", null);

        verifyNoInteractions(messageParser);
        verifyNoInteractions(notifier);
    }
}
