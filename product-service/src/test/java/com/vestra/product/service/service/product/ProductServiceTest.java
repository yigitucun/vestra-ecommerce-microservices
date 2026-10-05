package com.vestra.product.service.service.product;

import com.vestra.common.web.exception.ApiException;
import com.vestra.product.service.entity.Product;
import com.vestra.product.service.entity.Variant;
import com.vestra.product.service.repository.ProductRepository;
import com.vestra.product.service.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OutboxEventService eventService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldDeleteProductAndPublishVariantDeletedEvents() {
        UUID productId = UUID.randomUUID();
        UUID variant1Id = UUID.randomUUID();
        UUID variant2Id = UUID.randomUUID();

        Variant v1 = Variant.builder().id(variant1Id).sku("SKU-1").build();
        Variant v2 = Variant.builder().id(variant2Id).sku("SKU-2").build();

        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .variants(new HashSet<>(Set.of(v1, v2)))
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        verify(eventService).createVariantDeletedEvent(variant1Id.toString());
        verify(eventService).createVariantDeletedEvent(variant2Id.toString());
        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.delete(productId));
        verifyNoInteractions(eventService);
        verify(productRepository, never()).delete(any());
    }
}
