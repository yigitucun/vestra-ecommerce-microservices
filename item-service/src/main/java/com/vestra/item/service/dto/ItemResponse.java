package com.vestra.item.service.dto;

import com.vestra.item.service.entity.Item;

import java.time.Instant;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        UUID variantId,
        int quantity,
        int reserved,
        int availableStock,
        boolean active,
        Instant updatedAt
) {
    public static ItemResponse fromEntity(Item item) {
        int available = Math.max(0, item.getQuantity() - item.getReserved());
        return new ItemResponse(
                item.getId(),
                item.getVariantId(),
                item.getQuantity(),
                item.getReserved(),
                available,
                item.isActive(),
                item.getUpdatedAt()
        );
    }
}
