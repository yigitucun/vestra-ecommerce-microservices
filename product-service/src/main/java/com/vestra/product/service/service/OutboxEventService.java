package com.vestra.product.service.service;

import com.vestra.common.dto.OutboxEventDTO;
import com.vestra.common.event.payloads.VariantCreatedPayload;
import com.vestra.common.event.types.VariantEvents;
import com.vestra.product.service.entity.OutboxEvent;
import com.vestra.product.service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public void save(OutboxEventDTO event){
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(event.eventType())
                .aggregateType(event.aggregateType())
                .aggregateId(event.aggregateId())
                .payload(objectMapper.writeValueAsString(event.payload()))
                .build();
        eventRepository.save(outboxEvent);
    }

    public void createProductEvent(String aggregateId,String variantId,int initialStock){
        OutboxEventDTO outboxEventDTO = OutboxEventDTO.builder()
                .eventType(VariantEvents.VARIANT_CREATED)
                .aggregateType("Variant")
                .payload(new VariantCreatedPayload(variantId,initialStock))
                .aggregateId(aggregateId)
                .build();
        save(outboxEventDTO);
    }


}
