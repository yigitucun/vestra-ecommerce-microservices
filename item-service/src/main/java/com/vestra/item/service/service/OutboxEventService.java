package com.vestra.item.service.service;

import com.vestra.common.dto.OutboxEventDTO;
import com.vestra.common.event.payloads.OutOfStockPayload;
import com.vestra.common.event.payloads.StockReservedPayload;
import com.vestra.common.event.payloads.StockUpdatedPayload;
import com.vestra.common.event.types.ItemEvents;
import com.vestra.item.service.entity.OutboxEvent;
import com.vestra.item.service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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

    public void publishStockUpdatedEvent(String variantId, int availableStock, int quantity, int reserved) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(ItemEvents.STOCK_UPDATED)
                .aggregateType("Item")
                .aggregateId(variantId)
                .payload(new StockUpdatedPayload(variantId, availableStock, quantity, reserved))
                .build();
        save(event);
    }

    public void publishOutOfStockEvent(String variantId) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(ItemEvents.OUT_OF_STOCK)
                .aggregateType("Item")
                .aggregateId(variantId)
                .payload(new OutOfStockPayload(variantId))
                .build();
        save(event);
    }

    public void publishStockReservedEvent(String variantId, int reservedCount, int availableStock) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(ItemEvents.STOCK_RESERVED)
                .aggregateType("Item")
                .aggregateId(variantId)
                .payload(new StockReservedPayload(variantId, reservedCount, availableStock))
                .build();
        save(event);
    }

    public void publishStockReservedForOrderEvent(String orderId, String orderNumber) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(ItemEvents.STOCK_RESERVED_FOR_ORDER)
                .aggregateType("OrderStock")
                .aggregateId(orderId)
                .payload(new com.vestra.common.event.payloads.StockReservedForOrderPayload(orderId, orderNumber))
                .build();
        save(event);
    }

    public void publishStockReservationFailedEvent(String orderId, String orderNumber, String reason) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(ItemEvents.STOCK_RESERVATION_FAILED)
                .aggregateType("OrderStock")
                .aggregateId(orderId)
                .payload(new com.vestra.common.event.payloads.StockReservationFailedPayload(orderId, orderNumber, reason))
                .build();
        save(event);
    }
}
