package com.vestra.product.service.service.product;


import com.vestra.common.web.exception.ApiException;
import com.vestra.product.service.dto.product.CreateProductRequest;
import com.vestra.product.service.dto.variant.CreateVariantRequest;
import com.vestra.product.service.entity.Category;
import com.vestra.product.service.entity.Product;
import com.vestra.product.service.entity.Variant;
import com.vestra.product.service.entity.VariantAttribute;
import com.vestra.product.service.repository.CategoryRepository;
import com.vestra.product.service.repository.ProductRepository;
import com.vestra.product.service.service.OutboxEventService;
import com.vestra.product.service.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VariantRepository variantRepository;
    private final OutboxEventService eventService;

    @Transactional
    public void execute(CreateProductRequest request){
        Category category = this.categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ApiException.badRequest("Kategori Bulunamadı","Kategori bulunamadı"));

        if (this.productRepository.existsBySlug(request.slug())) {
            throw ApiException.conflict("Tekrarlanan Slug", "Bu slug ('" + request.slug() + "') değerine sahip bir ürün zaten mevcut.");
        }

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .category(category)
                .slug(request.slug())
                .imageUrl(request.imageUrl())
                .build();

        Set<String> requestSkus = new HashSet<>();

        for (CreateVariantRequest variantRequest: request.variants()){
            String sku = variantRequest.sku() != null ? variantRequest.sku().trim() : "";
            if (!requestSkus.add(sku.toLowerCase())) {
                throw ApiException.badRequest("Tekrarlanan SKU", "Aynı SKU kodu ('" + sku + "') birden fazla varyant için kullanılamaz.");
            }
            if (this.variantRepository.existsBySku(sku)) {
                throw ApiException.conflict("Mevcut SKU", "Bu SKU kodu ('" + sku + "') sistemde zaten kullanımda.");
            }

            Variant variant = Variant.builder()
                    .sku(sku)
                    .price(variantRequest.price())
                    .initialStock(variantRequest.initialStock())
                    .product(product)
                    .build();

            if (variantRequest.attributes() != null) {
                Set<String> seenAttributeNames = new HashSet<>();
                for (var attributesRequest : variantRequest.attributes()) {
                    String attrName = attributesRequest.attributeName() != null ? attributesRequest.attributeName().trim() : "";
                    String attrVal = attributesRequest.attributeValue() != null ? attributesRequest.attributeValue().trim() : "";
                    if (attrName.isEmpty() && attrVal.isEmpty()) {
                        continue;
                    }
                    if (!seenAttributeNames.add(attrName.toLowerCase())) {
                        throw ApiException.badRequest(
                                "Tekrarlanan Özellik",
                                "Bir varyant içerisinde aynı özellik adı ('" + attrName + "') birden fazla kez tanımlanamaz. Farklı renk veya beden gibi seçenekler için lütfen yeni bir varyant ekleyiniz."
                        );
                    }

                    VariantAttribute attribute = VariantAttribute.builder()
                            .attributeValue(attrVal)
                            .attributeName(attrName)
                            .variant(variant)
                            .build();
                    variant.getAttributes().add(attribute);
                }
            }

            product.getVariants().add(variant);
        }
        Product savedProduct = productRepository.save(product);
        savedProduct.getVariants().forEach(variant -> {
            eventService.createProductEvent(variant.getId().toString(),variant.getId().toString(),variant.getInitialStock());
        });
    }

}
