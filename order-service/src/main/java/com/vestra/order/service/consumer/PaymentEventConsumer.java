package com.vestra.order.service.consumer;

import com.vestra.common.event.payloads.PaymentCompletedPayload;
import com.vestra.common.event.payloads.PaymentFailedPayload;
import com.vestra.common.event.payloads.PaymentRefundedPayload;
import com.vestra.common.event.types.PaymentEvents;
import com.vestra.order.service.entity.OrderStatus;
import com.vestra.order.service.service.OrderService;
import com.vestra.order.service.utils.OutboxMessageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OutboxMessageParser messageParser;
    private final OrderService orderService;

    @KafkaListener(topics = "payment.events", groupId = "order-service-payment-group")
    public void consume(String message, @Header(name = "event_type", required = false) byte[] eventTypeHeader) {
        if (eventTypeHeader == null) {
            log.warn("Payment event_type header eksik!");
            return;
        }

        String eventType = new String(eventTypeHeader, StandardCharsets.UTF_8);
        try {
            switch (eventType) {
                case PaymentEvents.PAYMENT_COMPLETED -> handlePaymentCompleted(message);
                case PaymentEvents.PAYMENT_FAILED -> handlePaymentFailed(message);
                case PaymentEvents.PAYMENT_REFUNDED -> handlePaymentRefunded(message);
                default -> log.debug("İlgilenilmeyen payment event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Payment event işlenirken hata oluştu (eventType={})", eventType, e);
        }
    }

    private void handlePaymentCompleted(String message) {
        PaymentCompletedPayload payload = messageParser.parse(message, PaymentCompletedPayload.class);
        log.info("Ödeme başarılı, sipariş durumu güncelleniyor: orderId={}, txnId={}",
                payload.orderId(), payload.transactionId());
        orderService.updateOrderStatus(UUID.fromString(payload.orderId()), OrderStatus.PAID);
    }

    private void handlePaymentFailed(String message) {
        PaymentFailedPayload payload = messageParser.parse(message, PaymentFailedPayload.class);
        log.warn("Ödeme başarısız, sipariş iptal ediliyor ve kompensasyon tetikleniyor: orderId={}, reason={}",
                payload.orderId(), payload.failureReason());
        UUID orderId = UUID.fromString(payload.orderId());
        UUID userId = UUID.fromString(payload.userId());
        orderService.cancelOrder(orderId, userId, "Ödeme başarısız: " + payload.failureReason(), true);
    }

    private void handlePaymentRefunded(String message) {
        PaymentRefundedPayload payload = messageParser.parse(message, PaymentRefundedPayload.class);
        log.info("Ödeme iade edildi, sipariş iptal ediliyor: orderId={}", payload.orderId());
        orderService.updateOrderStatus(UUID.fromString(payload.orderId()), OrderStatus.CANCELLED);
    }
}
