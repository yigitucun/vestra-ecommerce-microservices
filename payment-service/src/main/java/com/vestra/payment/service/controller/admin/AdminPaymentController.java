package com.vestra.payment.service.controller.admin;

import com.vestra.common.dto.PaginatedResponse;
import com.vestra.payment.service.dto.PaymentResponse;
import com.vestra.payment.service.dto.RefundPaymentRequest;
import com.vestra.payment.service.entity.PaymentStatus;
import com.vestra.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PaymentStatus status
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PaginatedResponse<PaymentResponse> response = paymentService.getAllPayments(pageRequest, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RefundPaymentRequest request
    ) {
        PaymentResponse response = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(response);
    }
}
