package com.vestra.payment.service.controller;

import com.vestra.payment.service.dto.PaymentResponse;
import com.vestra.payment.service.dto.ProcessPaymentRequest;
import com.vestra.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        PaymentResponse response = paymentService.processPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(jwt.getClaimAsString("role"));
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(orderId, userId, isAdmin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(jwt.getClaimAsString("role"));
        PaymentResponse response = paymentService.getPaymentById(id, userId, isAdmin);
        return ResponseEntity.ok(response);
    }
}
