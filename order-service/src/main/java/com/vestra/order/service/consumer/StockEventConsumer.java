package com.vestra.order.service.consumer;

import com.vestra.common.event.payloads.StockReservationFailedPayload;
import com.vestra.common.event.payloads.StockReservedForOrderPayload;
import com.vestra.common.event.types.ItemEvents;
import com.vestra.order.service.entity.OrderStatus;
import com.vestra.order.service.service.OrderService;
import com.vestra.order.service.utils.OutboxMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEventConsumer {

    private final OutboxMessageParser messageParser;
    private final OrderService orderService;

    @KafkaListener(topics = "item.events", groupId = "order-service-stock-group")
    public void consume(String message, @Header(name = "event_type", required = false) byte[] eventTypeHeader) {
        if (eventTypeHeader == null) {
            log.warn("Item event_type header eksik!");
            return;
        }

        String eventType = new String(eventTypeHeader, StandardCharsets.UTF_8);
        try {
            switch (eventType) {
                case ItemEvents.STOCK_RESERVED_FOR_ORDER -> handleStockReserved(message);
                case ItemEvents.STOCK_RESERVATION_FAILED -> handleStockReservationFailed(message);
                default -> log.debug("İlgilenilmeyen item event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Item event işlenirken hata oluştu (eventType={})", eventType, e);
        }
    }

    private void handleStockReserved(String message) {
        StockReservedForOrderPayload payload = messageParser.parse(message, StockReservedForOrderPayload.class);
        log.info("Sipariş stokları rezerve edildi, durum güncelleniyor: orderId={}", payload.orderId());
        orderService.updateOrderStatus(UUID.fromString(payload.orderId()), OrderStatus.STOCK_CONFIRMED);
    }

    private void handleStockReservationFailed(String message) {
        StockReservationFailedPayload payload = messageParser.parse(message, StockReservationFailedPayload.class);
        log.warn("Sipariş stok rezervasyonu başarısız, sipariş iptal ediliyor: orderId={}, reason={}", payload.orderId(), payload.reason());
        orderService.updateOrderStatus(UUID.fromString(payload.orderId()), OrderStatus.CANCELLED);
    }
}
