package com.vestra.notification.service.consumer;

import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.event.types.OrderEvents;
import com.vestra.notification.service.service.notifier.OrderConfirmationNotifier;
import com.vestra.notification.service.utils.OutboxMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderNotificationConsumer {

    private final OutboxMessageParser messageParser;
    private final OrderConfirmationNotifier orderConfirmationNotifier;

    @KafkaListener(topics = "order.events", groupId = "notification-order-group")
    public void consume(String message, @Header(name = "event_type", required = false) byte[] eventTypeHeader) {
        if (eventTypeHeader == null) {
            log.warn("Order event type header eksik!");
            return;
        }

        String eventType = new String(eventTypeHeader, StandardCharsets.UTF_8);
        try {
            if (OrderEvents.ORDER_CREATED.equals(eventType)) {
                OrderCreatedPayload payload = messageParser.parse(message, OrderCreatedPayload.class);
                orderConfirmationNotifier.notify(payload);
            }
        } catch (Exception e) {
            log.error("Order notification eventi işlenirken hata oluştu (eventType={})", eventType, e);
        }
    }
}
