package com.vestra.item.service.service;

import com.vestra.item.service.entity.Item;
import com.vestra.item.service.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void createItem(String variantId,int initialStock){
        Item item = Item.builder()
                .variantId(UUID.fromString(variantId))
                .quantity(initialStock)
                .build();
        this.itemRepository.save(item);
        log.info("Item oluşturuldu (variantId={}, initialStock={})", variantId, initialStock);
    }

    @Transactional
    public void deleteItem(String variantId){
        UUID uuid = UUID.fromString(variantId);
        this.itemRepository.deleteByVariantId(uuid);
        log.info("Item silindi (variantId={})", variantId);
    }

}
