package com.vestra.order.service.client;

import com.vestra.common.web.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(@Value("${services.product-service-url:http://localhost:8082}") String productServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    public record VariantInfo(
            UUID variantId,
            UUID productId,
            String productName,
            String sku,
            BigDecimal price
    ) {}

    public Map<UUID, VariantInfo> getVariantsMap(List<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        try {
            List<VariantInfo> variants = restClient.post()
                    .uri("/api/products/variants/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(variantIds)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<VariantInfo>>() {});

            if (variants == null || variants.isEmpty()) {
                throw ApiException.badRequest("Ürün Bilgisi Bulunamadı", "Siparişteki ürünler product-service üzerinde bulunamadı.");
            }

            return variants.stream()
                    .collect(Collectors.toMap(VariantInfo::variantId, Function.identity()));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("product-service varyant sorgulama hatası: {}", e.getMessage());
            throw ApiException.badRequest(
                    "Ürün Doğrulama Hatası",
                    "Ürün bilgileri ve fiyatları doğrulanamadı. Lütfen tekrar deneyiniz."
            );
        }
    }
}
