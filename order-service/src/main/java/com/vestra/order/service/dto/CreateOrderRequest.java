package com.vestra.order.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank(message = "E-posta adresi boş bırakılamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String customerEmail,

        @NotBlank(message = "Teslimat adresi boş bırakılamaz")
        String shippingAddress,

        @NotEmpty(message = "Sipariş en az bir ürün içermelidir")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "Varyant ID zorunludur")
            UUID variantId,

            String productName,

            String sku,

            @Positive(message = "Adet 0'dan büyük olmalıdır")
            int quantity,

            BigDecimal unitPrice
    ) {}
}
