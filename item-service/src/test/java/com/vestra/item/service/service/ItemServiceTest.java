package com.vestra.item.service.service;

import com.vestra.item.service.entity.Item;
import com.vestra.item.service.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldCreateItem() {
        UUID variantId = UUID.randomUUID();
        itemService.createItem(variantId.toString(), 15);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());

        Item saved = captor.getValue();
        assertEquals(variantId, saved.getVariantId());
        assertEquals(15, saved.getQuantity());
    }

    @Test
    void shouldDeleteItemByVariantId() {
        UUID variantId = UUID.randomUUID();
        itemService.deleteItem(variantId.toString());

        verify(itemRepository).deleteByVariantId(variantId);
    }
}
