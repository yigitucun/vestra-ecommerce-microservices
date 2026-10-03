package com.vestra.product.service.service.product;

import com.vestra.product.service.projection.ProductProjection;
import com.vestra.product.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public PagedModel<ProductProjection> getAll(int page, int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by("id").descending());
        return new PagedModel<>(this.productRepository.findAllProjectedBy(pageable));
    }

    public void delete(UUID id){
        productRepository.deleteById(id);
    }






}
