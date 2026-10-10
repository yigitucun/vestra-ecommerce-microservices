package com.vestra.payment.service.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundPaymentRequest(
        @NotBlank(message = "İade gerekçesi zorunludur")
        String reason
) {}
