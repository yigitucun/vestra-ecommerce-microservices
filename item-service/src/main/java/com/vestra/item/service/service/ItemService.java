package com.vestra.item.service.service;

import com.vestra.common.web.exception.ApiException;
import com.vestra.item.service.dto.ItemResponse;
import com.vestra.item.service.dto.UpdateStockRequest;
import com.vestra.item.service.entity.Item;
import com.vestra.item.service.repository.ItemRepository;
import com.vestra.item.service.dto.StockMovementResponse;
import com.vestra.item.service.entity.StockMovement;
import com.vestra.item.service.entity.StockMovementType;
import com.vestra.item.service.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final OutboxEventService outboxEventService;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public void createItem(String variantId, int initialStock) {
        UUID uuid;
        try {
            uuid = UUID.fromString(variantId);
        } catch (IllegalArgumentException e) {
            log.error("Geçersiz variantId formatı: {}", variantId);
            return;
        }

        // Idempotency kontrolü: Aynı event tekrar geldiyse mükerrer kayıt oluşturma
        if (itemRepository.existsByVariantId(uuid)) {
            log.warn("Varyant için stok kaydı zaten mevcut, mükerrer event atlandı: variantId={}", variantId);
            return;
        }

        int stock = Math.max(0, initialStock);
        Item item = Item.builder()
                .variantId(uuid)
                .quantity(stock)
                .build();
        Item saved = this.itemRepository.save(item);
        log.info("Item oluşturuldu (variantId={}, initialStock={})", variantId, initialStock);

        stockMovementRepository.save(com.vestra.item.service.entity.StockMovement.builder()
                .item(saved)
                .variantId(uuid)
                .changeAmount(stock)
                .resultingQuantity(stock)
                .movementType(com.vestra.item.service.entity.StockMovementType.INITIAL_STOCK)
                .referenceId("EVENT_VARIANT_CREATED")
                .build());

        if (stock == 0) {
            outboxEventService.publishOutOfStockEvent(variantId);
        } else {
            outboxEventService.publishStockUpdatedEvent(variantId, stock, stock, 0);
        }
    }

    @Transactional
    public void deleteItem(String variantId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(variantId);
        } catch (IllegalArgumentException e) {
            log.error("Geçersiz variantId formatı: {}", variantId);
            return;
        }

        this.itemRepository.deleteByVariantId(uuid);
        log.info("Item silindi (variantId={})", variantId);
    }

    @Transactional(readOnly = true)
    public ItemResponse getByVariantId(UUID variantId) {
        return itemRepository.findByVariantId(variantId)
                .map(ItemResponse::fromEntity)
                .orElseThrow(() -> ApiException.notFound(
                        "Stok Bulunamadı",
                        "Belirtilen varyanta (" + variantId + ") ait stok kaydı bulunamadı."
                ));
    }

    @Transactional
    public ItemResponse updateStock(UUID variantId, UpdateStockRequest request) {
        Item item = itemRepository.findByVariantId(variantId)
                .orElseThrow(() -> ApiException.notFound(
                        "Stok Bulunamadı",
                        "Belirtilen varyanta (" + variantId + ") ait stok kaydı bulunamadı."
                ));

        int oldQuantity = item.getQuantity();
        item.setQuantity(request.quantity());
        if (request.active() != null) {
            item.setActive(request.active());
        }

        Item updated = itemRepository.save(item);
        int available = Math.max(0, updated.getQuantity() - updated.getReserved());
        int change = updated.getQuantity() - oldQuantity;
        log.info("Stok güncellendi: variantId={}, newQuantity={}, active={}, available={}", variantId, item.getQuantity(), item.isActive(), available);

        stockMovementRepository.save(StockMovement.builder()
                .item(updated)
                .variantId(variantId)
                .changeAmount(change)
                .resultingQuantity(updated.getQuantity())
                .movementType(StockMovementType.ADMIN_UPDATE)
                .referenceId("ADMIN_MANUAL_UPDATE")
                .build());

        outboxEventService.publishStockUpdatedEvent(variantId.toString(), available, updated.getQuantity(), updated.getReserved());
        if (available == 0 || !updated.isActive()) {
            outboxEventService.publishOutOfStockEvent(variantId.toString());
        }

        return ItemResponse.fromEntity(updated);
    }

    @Transactional
    public void reserveStock(UUID variantId, int count) {
        if (count <= 0) {
            throw ApiException.badRequest("Geçersiz Adet", "Rezervasyon adedi 0'dan büyük olmalıdır.");
        }

        Item item = itemRepository.findByVariantId(variantId)
                .orElseThrow(() -> ApiException.notFound("Stok Bulunamadı", "Varyant bulunamadı: " + variantId));

        if (!item.isActive()) {
            throw ApiException.badRequest("Stok Pasif", "Bu varyant şu an satışa kapalıdır.");
        }

        int available = item.getQuantity() - item.getReserved();
        if (available < count) {
            throw ApiException.badRequest("Yetersiz Stok", "İstenen adet kadar stok mevcut değil. Mevcut: " + available);
        }

        item.setReserved(item.getReserved() + count);
        Item saved = itemRepository.save(item);
        int remainingAvailable = Math.max(0, saved.getQuantity() - saved.getReserved());
        log.info("Stok rezerve edildi: variantId={}, count={}, newReserved={}", variantId, count, item.getReserved());

        stockMovementRepository.save(StockMovement.builder()
                .item(saved)
                .variantId(variantId)
                .changeAmount(-count)
                .resultingQuantity(saved.getQuantity())
                .movementType(StockMovementType.RESERVATION_CREATED)
                .referenceId("RESERVE_HOLD")
                .build());

        outboxEventService.publishStockReservedEvent(variantId.toString(), count, remainingAvailable);
        if (remainingAvailable == 0) {
            outboxEventService.publishOutOfStockEvent(variantId.toString());
        }
    }

    @Transactional
    public void releaseStock(UUID variantId, int count) {
        Item item = itemRepository.findByVariantId(variantId)
                .orElseThrow(() -> ApiException.notFound("Stok Bulunamadı", "Varyant bulunamadı: " + variantId));

        item.setReserved(Math.max(0, item.getReserved() - count));
        Item saved = itemRepository.save(item);
        int available = Math.max(0, saved.getQuantity() - saved.getReserved());
        log.info("Stok rezervasyonu serbest bırakıldı: variantId={}, count={}, available={}", variantId, count, available);

        stockMovementRepository.save(StockMovement.builder()
                .item(saved)
                .variantId(variantId)
                .changeAmount(count)
                .resultingQuantity(saved.getQuantity())
                .movementType(StockMovementType.RESERVATION_RELEASED)
                .referenceId("RESERVE_RELEASE")
                .build());

        outboxEventService.publishStockUpdatedEvent(variantId.toString(), available, saved.getQuantity(), saved.getReserved());
    }

    @Transactional
    public void commitStock(UUID variantId, int count) {
        Item item = itemRepository.findByVariantId(variantId)
                .orElseThrow(() -> ApiException.notFound("Stok Bulunamadı", "Varyant bulunamadı: " + variantId));

        item.setReserved(Math.max(0, item.getReserved() - count));
        item.setQuantity(Math.max(0, item.getQuantity() - count));
        Item saved = itemRepository.save(item);
        int available = Math.max(0, saved.getQuantity() - saved.getReserved());
        log.info("Stok düşüldü (satış kesinleşti): variantId={}, count={}, remainingQuantity={}", variantId, count, item.getQuantity());

        stockMovementRepository.save(StockMovement.builder()
                .item(saved)
                .variantId(variantId)
                .changeAmount(-count)
                .resultingQuantity(saved.getQuantity())
                .movementType(StockMovementType.RESERVATION_COMMITTED)
                .referenceId("ORDER_COMPLETED")
                .build());

        outboxEventService.publishStockUpdatedEvent(variantId.toString(), available, saved.getQuantity(), saved.getReserved());
        if (available == 0) {
            outboxEventService.publishOutOfStockEvent(variantId.toString());
        }
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByVariantId(UUID variantId) {
        return stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId)
                .stream()
                .map(StockMovementResponse::fromEntity)
                .toList();
    }
}
