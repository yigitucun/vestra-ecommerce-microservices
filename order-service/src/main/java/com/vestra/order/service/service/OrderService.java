package com.vestra.order.service.service;

import com.vestra.common.dto.PaginatedResponse;
import com.vestra.common.event.payloads.OrderCancelledPayload;
import com.vestra.common.event.payloads.OrderCreatedPayload;
import com.vestra.common.web.exception.ApiException;
import com.vestra.order.service.dto.CreateOrderRequest;
import com.vestra.order.service.dto.OrderResponse;
import com.vestra.order.service.entity.Order;
import com.vestra.order.service.entity.OrderItem;
import com.vestra.order.service.entity.OrderStatus;
import com.vestra.order.service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderCreatedPayload.OrderItemPayload> eventItemPayloads = new ArrayList<>();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .customerEmail(request.customerEmail())
                .shippingAddress(request.shippingAddress())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            BigDecimal subtotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variantId(itemReq.variantId())
                    .productName(itemReq.productName())
                    .sku(itemReq.sku())
                    .quantity(itemReq.quantity())
                    .unitPrice(itemReq.unitPrice())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);

            eventItemPayloads.add(new OrderCreatedPayload.OrderItemPayload(
                    itemReq.variantId().toString(),
                    itemReq.productName(),
                    itemReq.sku(),
                    itemReq.quantity(),
                    itemReq.unitPrice()
            ));
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Sipariş oluşturuldu: orderId={}, orderNumber={}, totalAmount={}", savedOrder.getId(), orderNumber, totalAmount);

        OrderCreatedPayload eventPayload = new OrderCreatedPayload(
                savedOrder.getId().toString(),
                orderNumber,
                userId.toString(),
                savedOrder.getCustomerEmail(),
                totalAmount,
                savedOrder.getShippingAddress(),
                eventItemPayloads
        );

        outboxEventService.publishOrderCreatedEvent(eventPayload);

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID currentUserId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Sipariş Bulunamadı", "Sipariş bulunamadı: " + orderId));

        if (!isAdmin && !order.getUserId().equals(currentUserId)) {
            throw ApiException.unAuthorized("Yetkisiz Erişim", "Bu siparişi görüntüleme yetkiniz yok.");
        }

        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<OrderResponse> getAllOrders(int page, int size, OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .toList();

        return new PaginatedResponse<>(
                content,
                new PaginatedResponse.PageInfo(
                        orderPage.getSize(),
                        orderPage.getNumber(),
                        orderPage.getTotalElements(),
                        orderPage.getTotalPages()
                )
        );
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Sipariş Bulunamadı", "Sipariş bulunamadı: " + orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Sipariş durumu güncellendi: orderId={}, newStatus={}", orderId, newStatus);
    }

    @Transactional
    public void cancelOrder(UUID orderId, UUID currentUserId, String reason, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Sipariş Bulunamadı", "Sipariş bulunamadı: " + orderId));

        if (!isAdmin && !order.getUserId().equals(currentUserId)) {
            throw ApiException.unAuthorized("Yetkisiz Erişim", "Bu siparişi iptal etme yetkiniz yok.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("Geçersiz İşlem", "Sipariş zaten iptal edilmiş.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw ApiException.badRequest("Geçersiz İşlem", "Tamamlanmış sipariş iptal edilemez.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Sipariş iptal edildi: orderId={}, reason={}", orderId, reason);

        List<OrderCancelledPayload.OrderItemReleasePayload> releaseItems = order.getItems().stream()
                .map(item -> new OrderCancelledPayload.OrderItemReleasePayload(
                        item.getVariantId().toString(),
                        item.getQuantity()
                ))
                .toList();

        outboxEventService.publishOrderCancelledEvent(new OrderCancelledPayload(
                order.getId().toString(),
                order.getOrderNumber(),
                reason,
                releaseItems
        ));
    }
}
