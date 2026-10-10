package com.vestra.payment.service.client;

import com.vestra.common.web.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@Value("${services.order-service-url:http://localhost:8083}") String orderServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public record OrderDetails(
            UUID orderId,
            String orderNumber,
            UUID userId,
            BigDecimal totalAmount,
            String status
    ) {}

    public OrderDetails getOrder(UUID orderId) {
        try {
            return restClient.get()
                    .uri("/api/internal/orders/{orderId}", orderId)
                    .retrieve()
                    .body(OrderDetails.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("order-service sorgulama hatası: orderId={}, err={}", orderId, e.getMessage());
            throw ApiException.notFound("Sipariş Bulunamadı", "Sipariş bilgileri order-service üzerinden doğrulanamadı: " + orderId);
        }
    }
}
