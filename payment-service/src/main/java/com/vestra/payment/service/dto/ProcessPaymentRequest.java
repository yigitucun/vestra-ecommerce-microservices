package com.vestra.payment.service.dto;

import com.vestra.payment.service.entity.PaymentMethod;
import jakarta.validation.constraints.AssertTrue;
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

        // PSP Hosted Form / Tokenization (PCI-DSS uyumlu güvenli yöntem)
        String paymentToken,
        String cardLastFour,

        // Doğrudan Kart Bilgileri (Geriye uyumluluk ve lokal sandbox simülasyonu)
        String cardNumber,
        String cardHolderName,
        String expireMonth,
        String expireYear,
        String cvv
) {
    // Geriye dönük uyumluluk constructor'ı (doğrudan kart simülasyonu için)
    public ProcessPaymentRequest(
            UUID orderId,
            String orderNumber,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String cardNumber,
            String cardHolderName,
            String expireMonth,
            String expireYear,
            String cvv
    ) {
        this(orderId, orderNumber, amount, paymentMethod, null, null, cardNumber, cardHolderName, expireMonth, expireYear, cvv);
    }

    // Tokenized PSP ödeme constructor'ı
    public ProcessPaymentRequest(
            UUID orderId,
            String orderNumber,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String paymentToken,
            String cardLastFour
    ) {
        this(orderId, orderNumber, amount, paymentMethod, paymentToken, cardLastFour, null, null, null, null, null);
    }

    @AssertTrue(message = "Geçerli bir ödeme işlemi için ya paymentToken (PSP Hosted Token) ya da kart bilgileri eksiksiz sağlanmalıdır")
    public boolean isValidPaymentInput() {
        boolean hasToken = paymentToken != null && !paymentToken.isBlank();
        boolean hasDirectCard = cardNumber != null && !cardNumber.isBlank()
                && cardHolderName != null && !cardHolderName.isBlank()
                && expireMonth != null && !expireMonth.isBlank()
                && expireYear != null && !expireYear.isBlank()
                && cvv != null && !cvv.isBlank();
        return hasToken || hasDirectCard;
    }

    @Override
    public String toString() {
        String maskedCard = cardNumber != null && cardNumber.length() >= 4
                ? "****-****-****-" + cardNumber.substring(cardNumber.length() - 4)
                : (cardNumber != null ? "****" : "null");
        return "ProcessPaymentRequest[" +
                "orderId=" + orderId +
                ", orderNumber=" + orderNumber +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", paymentToken=" + (paymentToken != null ? "[TOKEN_PROTECTED]" : "null") +
                ", cardLastFour=" + cardLastFour +
                ", cardNumber=" + maskedCard +
                ", cardHolderName=" + (cardHolderName != null ? "[MASKED]" : "null") +
                ", expireMonth=**" +
                ", expireYear=**" +
                ", cvv=***" +
                ']';
    }
}
