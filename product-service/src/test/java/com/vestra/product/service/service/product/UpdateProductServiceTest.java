package com.vestra.product.service.service.product;

import com.vestra.product.service.dto.product.UpdateProductRequest;
import com.vestra.product.service.dto.variant.UpdateVariantRequest;
import com.vestra.product.service.entity.Category;
import com.vestra.product.service.entity.Product;
import com.vestra.product.service.entity.Variant;
import com.vestra.product.service.repository.CategoryRepository;
import com.vestra.product.service.repository.ProductRepository;
import com.vestra.product.service.repository.VariantRepository;
import com.vestra.product.service.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private VariantRepository variantRepository;

    @Mock
    private OutboxEventService eventService;

    @InjectMocks
    private UpdateProductService updateProductService;

    @Test
    void shouldPublishVariantDeletedForRemovedVariantsAndProductEventForNewVariants() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID keptVariantId = UUID.randomUUID();
        UUID removedVariantId = UUID.randomUUID();

        Category category = Category.builder().id(categoryId).name("Test Category").build();

        Variant keptVariant = Variant.builder()
                .id(keptVariantId)
                .sku("SKU-KEPT")
                .price(BigDecimal.valueOf(100))
                .attributes(new HashSet<>())
                .build();

        Variant removedVariant = Variant.builder()
                .id(removedVariantId)
                .sku("SKU-REMOVED")
                .price(BigDecimal.valueOf(50))
                .attributes(new HashSet<>())
                .build();

        Set<Variant> initialVariants = new HashSet<>();
        initialVariants.add(keptVariant);
        initialVariants.add(removedVariant);

        Product product = Product.builder()
                .id(productId)
                .name("Old Product Name")
                .slug("old-slug")
                .category(category)
                .variants(initialVariants)
                .build();

        keptVariant.setProduct(product);
        removedVariant.setProduct(product);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(variantRepository.saveAndFlush(any(Variant.class))).thenAnswer(invocation -> {
            Variant v = invocation.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        UpdateVariantRequest keptVariantReq = new UpdateVariantRequest(
                keptVariantId,
                "SKU-KEPT",
                BigDecimal.valueOf(120),
                null,
                Collections.emptyList()
        );

        UpdateVariantRequest newVariantReq = new UpdateVariantRequest(
                null,
                "SKU-NEW",
                BigDecimal.valueOf(200),
                25,
                Collections.emptyList()
        );

        UpdateProductRequest request = new UpdateProductRequest(
                "New Product Name",
                "old-slug",
                "New Description",
                "http://example.com/img.png",
                categoryId,
                List.of(keptVariantReq, newVariantReq)
        );

        updateProductService.execute(productId, request);

        // Verify removed variant was published for deletion
        verify(eventService).createVariantDeletedEvent(removedVariantId.toString());

        // Verify new variant was published for creation with initial stock 25
        verify(eventService).createProductEvent(anyString(), anyString(), eq(25));

        // Verify product was saved
        verify(productRepository).save(product);
    }
}
