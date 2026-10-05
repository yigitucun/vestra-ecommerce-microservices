package com.vestra.product.service.service.product;

import com.vestra.common.web.exception.ApiException;
import com.vestra.product.service.entity.Product;
import com.vestra.product.service.entity.Variant;
import com.vestra.product.service.projection.ProductProjection;
import com.vestra.product.service.repository.ProductRepository;
import com.vestra.product.service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OutboxEventService eventService;

    public PagedModel<ProductProjection> getAll(int page, int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by("id").descending());
        return new PagedModel<>(this.productRepository.findAllProjectedBy(pageable));
    }

    @Transactional
    public void delete(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Ürün bulunamadı", "Silinmek istenen ürün bulunamadı."));

        if (product.getVariants() != null) {
            for (Variant variant : product.getVariants()) {
                if (variant.getId() != null) {
                    eventService.createVariantDeletedEvent(variant.getId().toString());
                }
            }
        }

        productRepository.delete(product);
    }






}
