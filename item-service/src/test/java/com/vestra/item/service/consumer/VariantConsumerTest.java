package com.vestra.item.service.consumer;

import com.vestra.common.event.payloads.VariantCreatedPayload;
import com.vestra.common.event.payloads.VariantDeletedPayload;
import com.vestra.common.event.types.VariantEvents;
import com.vestra.item.service.service.ItemService;
import com.vestra.item.service.utils.OutboxMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariantConsumerTest {

    @Mock
    private OutboxMessageParser messageParser;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private VariantConsumer variantConsumer;

    @Test
    void shouldHandleVariantCreated() {
        String rawMessage = "{\"variantId\":\"abc\",\"initialStock\":10}";
        byte[] header = VariantEvents.VARIANT_CREATED.getBytes(StandardCharsets.UTF_8);

        when(messageParser.parse(rawMessage, VariantCreatedPayload.class))
                .thenReturn(new VariantCreatedPayload("abc", 10));

        variantConsumer.consume(rawMessage, header);

        verify(itemService).createItem("abc", 10);
    }

    @Test
    void shouldHandleVariantDeleted() {
        String rawMessage = "{\"variantId\":\"abc\"}";
        byte[] header = VariantEvents.VARIANT_DELETED.getBytes(StandardCharsets.UTF_8);

        when(messageParser.parse(rawMessage, VariantDeletedPayload.class))
                .thenReturn(new VariantDeletedPayload("abc"));

        variantConsumer.consume(rawMessage, header);

        verify(itemService).deleteItem("abc");
    }
}
