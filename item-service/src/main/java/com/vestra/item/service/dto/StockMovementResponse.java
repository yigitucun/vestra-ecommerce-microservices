package com.vestra.item.service.dto;

import com.vestra.item.service.entity.StockMovement;
import com.vestra.item.service.entity.StockMovementType;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID variantId,
        int changeAmount,
        int resultingQuantity,
        StockMovementType movementType,
        String referenceId,
        Instant createdAt
) {
    public static StockMovementResponse fromEntity(StockMovement m) {
        return new StockMovementResponse(
                m.getId(),
                m.getVariantId(),
                m.getChangeAmount(),
                m.getResultingQuantity(),
                m.getMovementType(),
                m.getReferenceId(),
                m.getCreatedAt()
        );
    }
}
