package com.vestra.order.service.service;

import com.vestra.common.dto.OutboxEventDTO;
import com.vestra.common.event.payloads.OrderCancelledPayload;
import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.event.types.OrderEvents;
import com.vestra.order.service.entity.OutboxEvent;
import com.vestra.order.service.repository.OutboxEventRepository;
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

    public void publishOrderCreatedEvent(OrderCreatedPayload payload) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(OrderEvents.ORDER_CREATED)
                .aggregateType("Order")
                .aggregateId(payload.orderId())
                .payload(payload)
                .build();
        save(event);
    }

    public void publishOrderCancelledEvent(OrderCancelledPayload payload) {
        OutboxEventDTO event = OutboxEventDTO.builder()
                .eventType(OrderEvents.ORDER_CANCELLED)
                .aggregateType("Order")
                .aggregateId(payload.orderId())
                .payload(payload)
                .build();
        save(event);
    }
}
