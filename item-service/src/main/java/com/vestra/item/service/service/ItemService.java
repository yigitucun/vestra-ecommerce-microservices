package com.vestra.item.service.service;

import com.vestra.item.service.entity.Item;
import com.vestra.item.service.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public void createItem(String variantId,int initialStock){
        Item item = Item.builder()
                .variantId(UUID.fromString(variantId))
                .quantity(initialStock)
                .build();
        this.itemRepository.save(item);
    }

}
