package com.vestra.product.service.service.product;

import com.vestra.common.web.exception.ApiException;
import com.vestra.product.service.dto.product.UpdateProductRequest;
import com.vestra.product.service.dto.variant.UpdateVariantRequest;
import com.vestra.product.service.entity.Category;
import com.vestra.product.service.entity.Product;
import com.vestra.product.service.entity.Variant;
import com.vestra.product.service.entity.VariantAttribute;
import com.vestra.product.service.repository.CategoryRepository;
import com.vestra.product.service.repository.ProductRepository;
import com.vestra.product.service.repository.VariantRepository;
import com.vestra.product.service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VariantRepository variantRepository;
    private final OutboxEventService eventService;

    @Transactional
    public void execute(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Ürün bulunamadı", "Güncellenmek istenen ürün bulunamadı."));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ApiException.badRequest("Kategori bulunamadı", "Seçilen kategori bulunamadı."));

        String newSlug = request.slug().trim();
        if (!product.getSlug().equalsIgnoreCase(newSlug) && productRepository.existsBySlug(newSlug)) {
            throw ApiException.conflict("Slug kullanımda", "Bu slug değeri ('" + newSlug + "') başka bir ürün tarafından kullanılmaktadır.");
        }

        product.setName(request.name().trim());
        product.setSlug(newSlug);
        product.setDescription(request.description() != null ? request.description().trim() : null);
        product.setImageUrl(request.imageUrl() != null ? request.imageUrl().trim() : null);
        product.setCategory(category);

        Map<UUID, Variant> existingVariantMap = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(Variant::getId, Function.identity()));

        Set<String> requestSkus = new HashSet<>();
        Set<UUID> keptVariantIds = new HashSet<>();
        List<Variant> newVariantsToPublish = new ArrayList<>();

        for (UpdateVariantRequest variantReq : request.variants()) {
            String sku = variantReq.sku() != null ? variantReq.sku().trim() : "";
            if (sku.isEmpty()) {
                throw ApiException.badRequest("Geçersiz SKU", "Varyant için SKU boş olamaz.");
            }
            if (!requestSkus.add(sku.toLowerCase())) {
                throw ApiException.badRequest("Tekrarlanan SKU", "Aynı SKU kodu ('" + sku + "') birden fazla varyant için kullanılamaz.");
            }

            if (variantReq.id() != null && existingVariantMap.containsKey(variantReq.id())) {
                // Mevcut varyant güncelleme
                Variant variant = existingVariantMap.get(variantReq.id());
                if (!variant.getSku().equalsIgnoreCase(sku) && variantRepository.existsBySku(sku)) {
                    throw ApiException.conflict("Mevcut SKU", "Bu SKU kodu ('" + sku + "') sistemde zaten kullanımda.");
                }
                variant.setSku(sku);
                variant.setPrice(variantReq.price());

                // Nitelikleri yerinde güncelle (Hibernate duplicate key hatasını önlemek için)
                syncAttributes(variant, variantReq);
                keptVariantIds.add(variant.getId());
            } else {
                // Yeni varyant ekleme
                if (variantRepository.existsBySku(sku)) {
                    throw ApiException.conflict("Mevcut SKU", "Bu SKU kodu ('" + sku + "') sistemde zaten kullanımda.");
                }
                int initialStock = variantReq.initialStock() != null ? variantReq.initialStock() : 0;
                Variant newVariant = Variant.builder()
                        .sku(sku)
                        .price(variantReq.price())
                        .initialStock(initialStock)
                        .product(product)
                        .build();
                syncAttributes(newVariant, variantReq);
                product.getVariants().add(newVariant);
                newVariantsToPublish.add(newVariant);
            }
        }

        // Çıkarılan varyantları kaldır
        product.getVariants().removeIf(v -> v.getId() != null && !keptVariantIds.contains(v.getId()));

        Product savedProduct = productRepository.save(product);

        // Yeni eklenen varyantlar için item-service'e event gönder
        for (Variant newVariant : newVariantsToPublish) {
            if (newVariant.getId() != null) {
                eventService.createProductEvent(
                        newVariant.getId().toString(),
                        newVariant.getId().toString(),
                        newVariant.getInitialStock()
                );
            }
        }
    }

    private void syncAttributes(Variant variant, UpdateVariantRequest variantReq) {
        if (variantReq.attributes() == null) {
            variant.getAttributes().clear();
            return;
        }

        // Mevcut nitelikleri attribute_name bazında eşle
        Map<String, VariantAttribute> existingAttrMap = variant.getAttributes().stream()
                .filter(a -> a.getAttributeName() != null)
                .collect(Collectors.toMap(
                        a -> a.getAttributeName().trim().toLowerCase(),
                        Function.identity(),
                        (existing, duplicate) -> existing
                ));

        Set<String> processedAttrNames = new HashSet<>();

        for (var attrReq : variantReq.attributes()) {
            String attrName = attrReq.attributeName() != null ? attrReq.attributeName().trim() : "";
            String attrVal = attrReq.attributeValue() != null ? attrReq.attributeValue().trim() : "";
            if (attrName.isEmpty() && attrVal.isEmpty()) {
                continue;
            }

            String key = attrName.toLowerCase();
            if (!processedAttrNames.add(key)) {
                throw ApiException.badRequest(
                        "Tekrarlanan Özellik",
                        "Bir varyant içerisinde aynı özellik adı ('" + attrName + "') birden fazla kez tanımlanamaz. Farklı renk veya beden gibi seçenekler için lütfen yeni bir varyant ekleyiniz."
                );
            }

            if (existingAttrMap.containsKey(key)) {
                // Mevcut niteliği yerinde güncelle (INSERT yerine UPDATE yapar, unique constraint bozulmaz)
                VariantAttribute existingAttr = existingAttrMap.get(key);
                existingAttr.setAttributeName(attrName);
                existingAttr.setAttributeValue(attrVal);
            } else {
                // Yeni nitelik ekle
                VariantAttribute newAttr = VariantAttribute.builder()
                        .attributeName(attrName)
                        .attributeValue(attrVal)
                        .variant(variant)
                        .build();
                variant.getAttributes().add(newAttr);
            }
        }

        // İstekten kaldırılmış olan eski nitelikleri temizle
        variant.getAttributes().removeIf(a -> a.getAttributeName() != null && !processedAttrNames.contains(a.getAttributeName().trim().toLowerCase()));
    }
}
