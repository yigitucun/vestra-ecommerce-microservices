package com.vestra.item.service.controller;

import com.vestra.item.service.dto.ItemResponse;
import com.vestra.item.service.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{variantId}")
    public ResponseEntity<ItemResponse> getStock(@PathVariable UUID variantId) {
        return ResponseEntity.ok(itemService.getByVariantId(variantId));
    }
}
