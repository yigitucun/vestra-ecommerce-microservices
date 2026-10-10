package com.vestra.order.service.service;

import com.vestra.common.web.exception.ApiException;
import com.vestra.order.service.client.ProductServiceClient;
import com.vestra.order.service.dto.CreateOrderRequest;
import com.vestra.order.service.dto.OrderResponse;
import com.vestra.order.service.entity.Order;
import com.vestra.order.service.entity.OrderStatus;
import com.vestra.order.service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndPublishOutboxEvent() {
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        ProductServiceClient.VariantInfo variantInfo = new ProductServiceClient.VariantInfo(
                variantId,
                UUID.randomUUID(),
                "Akıllı Saat",
                "WATCH-001",
                BigDecimal.valueOf(1500)
        );

        when(productServiceClient.getVariantsMap(List.of(variantId)))
                .thenReturn(Map.of(variantId, variantInfo));

        CreateOrderRequest request = new CreateOrderRequest(
                "musteri@vestra.com",
                "Kadıköy, İstanbul",
                List.of(new CreateOrderRequest.OrderItemRequest(
                        variantId,
                        "Akıllı Saat",
                        "WATCH-001",
                        2,
                        BigDecimal.valueOf(1500)
                ))
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        OrderResponse response = orderService.createOrder(userId, request);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(BigDecimal.valueOf(3000), response.totalAmount());
        assertEquals("musteri@vestra.com", response.customerEmail());
        assertEquals("Kadıköy, İstanbul", response.shippingAddress());
        assertEquals(1, response.items().size());

        verify(orderRepository).save(any(Order.class));
        verify(outboxEventService).publishOrderCreatedEvent(any());
    }

    @Test
    void shouldRejectOrderWhenPriceIsTampered() {
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        ProductServiceClient.VariantInfo variantInfo = new ProductServiceClient.VariantInfo(
                variantId,
                UUID.randomUUID(),
                "Akıllı Saat",
                "WATCH-001",
                BigDecimal.valueOf(1500)
        );

        when(productServiceClient.getVariantsMap(List.of(variantId)))
                .thenReturn(Map.of(variantId, variantInfo));

        // Saldırgan 1500 TL yerine 10 TL gönderiyor
        CreateOrderRequest request = new CreateOrderRequest(
                "hacker@vestra.com",
                "Bilinmeyen Adres",
                List.of(new CreateOrderRequest.OrderItemRequest(
                        variantId,
                        "Akıllı Saat",
                        "WATCH-001",
                        1,
                        BigDecimal.valueOf(10)
                ))
        );

        ApiException ex = assertThrows(ApiException.class, () -> orderService.createOrder(userId, request));
        assertEquals("Fiyat Uyuşmazlığı", ex.getTitle());
        verify(orderRepository, never()).save(any());
        verify(outboxEventService, never()).publishOrderCreatedEvent(any());
    }

    @Test
    void shouldAllowOrderWhenUnitPriceIsOmittedAndUseAuthoritativePrice() {
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        ProductServiceClient.VariantInfo variantInfo = new ProductServiceClient.VariantInfo(
                variantId,
                UUID.randomUUID(),
                "Akıllı Saat",
                "WATCH-001",
                BigDecimal.valueOf(1500)
        );

        when(productServiceClient.getVariantsMap(List.of(variantId)))
                .thenReturn(Map.of(variantId, variantInfo));

        // İstemci unitPrice göndermiyor (null)
        CreateOrderRequest request = new CreateOrderRequest(
                "musteri@vestra.com",
                "Kadıköy, İstanbul",
                List.of(new CreateOrderRequest.OrderItemRequest(
                        variantId,
                        null,
                        null,
                        2,
                        null
                ))
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        OrderResponse response = orderService.createOrder(userId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(3000), response.totalAmount());
        assertEquals("Akıllı Saat", response.items().get(0).productName());
        assertEquals("WATCH-001", response.items().get(0).sku());
    }

    @Test
    void shouldRejectOrderWhenVariantNotFound() {
        UUID userId = UUID.randomUUID();
        UUID invalidVariantId = UUID.randomUUID();

        when(productServiceClient.getVariantsMap(List.of(invalidVariantId)))
                .thenReturn(Map.of());

        CreateOrderRequest request = new CreateOrderRequest(
                "musteri@vestra.com",
                "Kadıköy, İstanbul",
                List.of(new CreateOrderRequest.OrderItemRequest(
                        invalidVariantId,
                        null,
                        null,
                        1,
                        null
                ))
        );

        ApiException ex = assertThrows(ApiException.class, () -> orderService.createOrder(userId, request));
        assertEquals("Geçersiz Ürün", ex.getTitle());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldGetOrderByIdWhenOwner() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .userId(userId)
                .customerEmail("musteri@vestra.com")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(500))
                .shippingAddress("Beşiktaş")
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId, userId, false);
        assertNotNull(response);
        assertEquals("ORD-123", response.orderNumber());
    }

    @Test
    void shouldThrowUnauthorizedWhenNotOwnerAndNotAdmin() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .customerEmail("musteri@vestra.com")
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ApiException.class, () -> orderService.getOrderById(orderId, otherUser, false));
    }

    @Test
    void shouldCancelOrderAndPublishEvent() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-999")
                .userId(userId)
                .customerEmail("musteri@vestra.com")
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(orderId, userId, "Vazgeçildi", false);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        verify(outboxEventService).publishOrderCancelledEvent(any());
    }

    @Test
    void shouldUpdateOrderStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(orderId, OrderStatus.STOCK_CONFIRMED);

        assertEquals(OrderStatus.STOCK_CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
    }
}
