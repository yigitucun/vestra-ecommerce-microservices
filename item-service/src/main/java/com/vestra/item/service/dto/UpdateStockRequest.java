package com.vestra.item.service.dto;

import jakarta.validation.constraints.Min;

public record UpdateStockRequest(
        @Min(value = 0, message = "Stok miktarı 0 veya daha büyük olmalıdır")
        int quantity,
        Boolean active
) {
}
