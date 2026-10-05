package com.vestra.item.service.consumer;

import com.vestra.common.event.payloads.VariantCreatedPayload;
import com.vestra.common.event.payloads.VariantDeletedPayload;
import com.vestra.common.event.types.VariantEvents;
import com.vestra.item.service.service.ItemService;
import com.vestra.item.service.utils.OutboxMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
@Slf4j
public class VariantConsumer {

    private final OutboxMessageParser messageParser;
    private final ItemService itemService;

    @KafkaListener(topics = "product.events",groupId = "item-group")
    public void consume(String message, @Header(name = "event_type",required = false) byte[] eventTypeHeader){
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null ;
        try{
            switch (eventType){
                case VariantEvents.VARIANT_CREATED -> handleVariantCreated(message);
                case VariantEvents.VARIANT_DELETED -> handleVariantDeleted(message);
                case null, default -> log.warn("Bilinmeyen event type: {}", eventType);
            }
        }catch (Exception e){
            log.error("Event işlenirken hata oluştu (eventType={})", eventType, e);
        }
    }
    private void handleVariantCreated(String message){
        VariantCreatedPayload payload = messageParser.parse(message,VariantCreatedPayload.class);
        itemService.createItem(payload.variantId(),payload.initialStock());
    }

    private void handleVariantDeleted(String message){
        VariantDeletedPayload payload = messageParser.parse(message, VariantDeletedPayload.class);
        itemService.deleteItem(payload.variantId());
    }

}
