package com.vestra.item.service.consumer;

import com.vestra.common.event.payloads.OrderCancelledPayload;
import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.event.types.OrderEvents;
import com.vestra.common.web.exception.ApiException;
import com.vestra.item.service.service.ItemService;
import com.vestra.item.service.service.OutboxEventService;
import com.vestra.item.service.utils.OutboxMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OutboxMessageParser messageParser;
    private final ItemService itemService;
    private final OutboxEventService outboxEventService;

    @KafkaListener(topics = "order.events", groupId = "order-stock-group")
    public void consume(String message, @Header(name = "event_type", required = false) byte[] eventTypeHeader) {
        if (eventTypeHeader == null) {
            log.warn("Order event_type header eksik!");
            return;
        }

        String eventType = new String(eventTypeHeader, StandardCharsets.UTF_8);
        try {
            switch (eventType) {
                case OrderEvents.ORDER_CREATED -> handleOrderCreated(message);
                case OrderEvents.ORDER_CANCELLED -> handleOrderCancelled(message);
                default -> log.debug("İlgilenilmeyen order event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Order event işlenirken hata oluştu (eventType={})", eventType, e);
        }
    }

    private void handleOrderCreated(String message) {
        OrderCreatedPayload payload = messageParser.parse(message, OrderCreatedPayload.class);
        log.info("Sipariş için stok rezervasyonu başlatılıyor: orderId={}, orderNumber={}", payload.orderId(), payload.orderNumber());

        List<OrderCreatedPayload.OrderItemPayload> reservedItems = new ArrayList<>();
        boolean success = true;
        String failureReason = null;

        for (OrderCreatedPayload.OrderItemPayload item : payload.items()) {
            try {
                itemService.reserveStock(UUID.fromString(item.variantId()), item.quantity());
                reservedItems.add(item);
            } catch (ApiException e) {
                log.warn("Stok rezervasyonu başarısız: variantId={}, reason={}", item.variantId(), e.getDetail());
                success = false;
                failureReason = "Yetersiz stok: " + item.productName();
                break;
            } catch (Exception e) {
                log.error("Beklenmeyen stok rezervasyon hatası: variantId={}", item.variantId(), e);
                success = false;
                failureReason = "Sistem hatası: " + item.productName();
                break;
            }
        }

        if (success) {
            log.info("Tüm kalemler rezerve edildi: orderId={}", payload.orderId());
            outboxEventService.publishStockReservedForOrderEvent(payload.orderId(), payload.orderNumber());
        } else {
            log.warn("Rezervasyon başarısız, rezerve edilenler serbest bırakılıyor: orderId={}", payload.orderId());
            for (OrderCreatedPayload.OrderItemPayload reserved : reservedItems) {
                try {
                    itemService.releaseStock(UUID.fromString(reserved.variantId()), reserved.quantity());
                } catch (Exception e) {
                    log.error("Telafi edici işlem sırasında hata: variantId={}", reserved.variantId(), e);
                }
            }
            outboxEventService.publishStockReservationFailedEvent(payload.orderId(), payload.orderNumber(), failureReason);
        }
    }

    private void handleOrderCancelled(String message) {
        OrderCancelledPayload payload = messageParser.parse(message, OrderCancelledPayload.class);
        log.info("İptal edilen sipariş için stoklar serbest bırakılıyor: orderId={}", payload.orderId());

        if (payload.items() != null) {
            for (OrderCancelledPayload.OrderItemReleasePayload item : payload.items()) {
                try {
                    itemService.releaseStock(UUID.fromString(item.variantId()), item.quantity());
                } catch (Exception e) {
                    log.error("İptal sonrası stok serbest bırakılamadı: variantId={}", item.variantId(), e);
                }
            }
        }
    }
}
