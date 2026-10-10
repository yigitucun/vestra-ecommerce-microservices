package com.vestra.item.service.controller.admin;

import com.vestra.item.service.dto.ItemResponse;
import com.vestra.item.service.dto.UpdateStockRequest;
import com.vestra.item.service.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    @GetMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponse> getStock(@PathVariable UUID variantId) {
        return ResponseEntity.ok(itemService.getByVariantId(variantId));
    }

    @PutMapping("/{variantId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponse> updateStock(
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return ResponseEntity.ok(itemService.updateStock(variantId, request));
    }

    @GetMapping("/{variantId}/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<com.vestra.item.service.dto.StockMovementResponse>> getMovements(
            @PathVariable UUID variantId
    ) {
        return ResponseEntity.ok(itemService.getMovementsByVariantId(variantId));
    }
}
