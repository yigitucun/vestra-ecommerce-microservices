package com.vestra.item.service.consumer;

import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.event.types.OrderEvents;
import com.vestra.common.web.exception.ApiException;
import com.vestra.item.service.service.ItemService;
import com.vestra.item.service.service.OutboxEventService;
import com.vestra.item.service.utils.OutboxMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @Mock
    private OutboxMessageParser messageParser;

    @Mock
    private ItemService itemService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private OrderConsumer orderConsumer;

    @Test
    void shouldReserveAllItemsWhenOrderCreated() {
        UUID variantId = UUID.randomUUID();
        String rawMessage = "{}";
        byte[] header = OrderEvents.ORDER_CREATED.getBytes(StandardCharsets.UTF_8);

        OrderCreatedPayload payload = new OrderCreatedPayload(
                "order-1",
                "ORD-100",
                "user-1",
                "user@vestra.com",
                BigDecimal.valueOf(200),
                "Adres",
                List.of(new OrderCreatedPayload.OrderItemPayload(
                        variantId.toString(),
                        "T-Shirt",
                        "TSHIRT-RED",
                        2,
                        BigDecimal.valueOf(100)
                ))
        );

        when(messageParser.parse(rawMessage, OrderCreatedPayload.class)).thenReturn(payload);

        orderConsumer.consume(rawMessage, header);

        verify(itemService).reserveStock(variantId, 2);
        verify(outboxEventService).publishStockReservedForOrderEvent("order-1", "ORD-100");
    }

    @Test
    void shouldReleaseAndFailWhenStockInsufficient() {
        UUID variant1 = UUID.randomUUID();
        UUID variant2 = UUID.randomUUID();
        String rawMessage = "{}";
        byte[] header = OrderEvents.ORDER_CREATED.getBytes(StandardCharsets.UTF_8);

        OrderCreatedPayload payload = new OrderCreatedPayload(
                "order-2",
                "ORD-200",
                "user-1",
                "user@vestra.com",
                BigDecimal.valueOf(500),
                "Adres",
                List.of(
                        new OrderCreatedPayload.OrderItemPayload(variant1.toString(), "Item 1", "SKU1", 1, BigDecimal.valueOf(200)),
                        new OrderCreatedPayload.OrderItemPayload(variant2.toString(), "Item 2", "SKU2", 5, BigDecimal.valueOf(300))
                )
        );

        when(messageParser.parse(rawMessage, OrderCreatedPayload.class)).thenReturn(payload);
        doNothing().when(itemService).reserveStock(variant1, 1);
        doThrow(ApiException.badRequest("Yetersiz Stok", "Yetersiz")).when(itemService).reserveStock(variant2, 5);

        orderConsumer.consume(rawMessage, header);

        // variant1 should be rolled back (compensating transaction)
        verify(itemService).releaseStock(variant1, 1);
        verify(outboxEventService).publishStockReservationFailedEvent(eq("order-2"), eq("ORD-200"), any());
    }
}
