package com.vestra.payment.service.dto;

import com.vestra.payment.service.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull(message = "Sipariş ID zorunludur")
        UUID orderId,

        @NotBlank(message = "Sipariş numarası zorunludur")
        String orderNumber,

        @NotNull(message = "Ödeme tutarı zorunludur")
        @DecimalMin(value = "0.01", message = "Ödeme tutarı 0'dan büyük olmalıdır")
        BigDecimal amount,

        @NotNull(message = "Ödeme yöntemi zorunludur")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Kart numarası zorunludur")
        String cardNumber,

        @NotBlank(message = "Kart üzerindeki isim zorunludur")
        String cardHolderName,

        @NotBlank(message = "Son kullanma ayı zorunludur")
        String expireMonth,

        @NotBlank(message = "Son kullanma yılı zorunludur")
        String expireYear,

        @NotBlank(message = "CVV zorunludur")
        String cvv
) {}
