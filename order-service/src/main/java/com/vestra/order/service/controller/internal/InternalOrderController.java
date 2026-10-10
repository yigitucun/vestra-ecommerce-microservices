package com.vestra.order.service.controller.internal;

import com.vestra.order.service.dto.OrderVerificationResponse;
import com.vestra.order.service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderVerificationResponse> getOrderForVerification(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderForVerification(orderId));
    }
}
