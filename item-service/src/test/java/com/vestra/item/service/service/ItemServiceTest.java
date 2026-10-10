package com.vestra.item.service.service;

import com.vestra.common.web.exception.ApiException;
import com.vestra.item.service.dto.ItemResponse;
import com.vestra.item.service.dto.UpdateStockRequest;
import com.vestra.item.service.entity.Item;
import com.vestra.item.service.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private com.vestra.item.service.repository.StockMovementRepository stockMovementRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldCreateItemWhenNotExists() {
        UUID variantId = UUID.randomUUID();
        when(itemRepository.existsByVariantId(variantId)).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.createItem(variantId.toString(), 15);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());

        Item saved = captor.getValue();
        assertEquals(variantId, saved.getVariantId());
        assertEquals(15, saved.getQuantity());
    }

    @Test
    void shouldSkipCreateItemWhenAlreadyExists() {
        UUID variantId = UUID.randomUUID();
        when(itemRepository.existsByVariantId(variantId)).thenReturn(true);

        itemService.createItem(variantId.toString(), 15);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void shouldDeleteItemByVariantId() {
        UUID variantId = UUID.randomUUID();
        itemService.deleteItem(variantId.toString());

        verify(itemRepository).deleteByVariantId(variantId);
    }

    @Test
    void shouldGetByVariantId() {
        UUID variantId = UUID.randomUUID();
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .variantId(variantId)
                .quantity(20)
                .reserved(5)
                .active(true)
                .build();

        when(itemRepository.findByVariantId(variantId)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.getByVariantId(variantId);
        assertNotNull(response);
        assertEquals(variantId, response.variantId());
        assertEquals(20, response.quantity());
        assertEquals(5, response.reserved());
        assertEquals(15, response.availableStock());
    }

    @Test
    void shouldThrowNotFoundWhenItemDoesNotExist() {
        UUID variantId = UUID.randomUUID();
        when(itemRepository.findByVariantId(variantId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> itemService.getByVariantId(variantId));
    }

    @Test
    void shouldUpdateStock() {
        UUID variantId = UUID.randomUUID();
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .variantId(variantId)
                .quantity(10)
                .active(true)
                .build();

        when(itemRepository.findByVariantId(variantId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStockRequest request = new UpdateStockRequest(50, false);
        ItemResponse response = itemService.updateStock(variantId, request);

        assertEquals(50, response.quantity());
        assertFalse(response.active());
    }

    @Test
    void shouldReserveAndReleaseStock() {
        UUID variantId = UUID.randomUUID();
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .variantId(variantId)
                .quantity(10)
                .reserved(2)
                .active(true)
                .build();

        when(itemRepository.findByVariantId(variantId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 3 adet daha rezerve et
        itemService.reserveStock(variantId, 3);
        assertEquals(5, item.getReserved());
        verify(outboxEventService).publishStockReservedEvent(variantId.toString(), 3, 5);

        // 2 adet serbest bırak
        itemService.releaseStock(variantId, 2);
        assertEquals(3, item.getReserved());
        verify(outboxEventService).publishStockUpdatedEvent(variantId.toString(), 7, 10, 3);
    }

    @Test
    void shouldCommitStockAndPublishEvents() {
        UUID variantId = UUID.randomUUID();
        Item item = Item.builder()
                .id(UUID.randomUUID())
                .variantId(variantId)
                .quantity(5)
                .reserved(5)
                .active(true)
                .build();

        when(itemRepository.findByVariantId(variantId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.commitStock(variantId, 5);
        assertEquals(0, item.getQuantity());
        assertEquals(0, item.getReserved());

        verify(outboxEventService).publishStockUpdatedEvent(variantId.toString(), 0, 0, 0);
        verify(outboxEventService).publishOutOfStockEvent(variantId.toString());
    }

    @Test
    void shouldGetMovementsByVariantId() {
        UUID variantId = UUID.randomUUID();
        com.vestra.item.service.entity.StockMovement movement = com.vestra.item.service.entity.StockMovement.builder()
                .id(UUID.randomUUID())
                .variantId(variantId)
                .changeAmount(10)
                .resultingQuantity(10)
                .movementType(com.vestra.item.service.entity.StockMovementType.INITIAL_STOCK)
                .referenceId("TEST")
                .build();

        when(stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId))
                .thenReturn(java.util.List.of(movement));

        var movements = itemService.getMovementsByVariantId(variantId);
        assertEquals(1, movements.size());
        assertEquals(10, movements.get(0).changeAmount());
        assertEquals(com.vestra.item.service.entity.StockMovementType.INITIAL_STOCK, movements.get(0).movementType());
    }
}

