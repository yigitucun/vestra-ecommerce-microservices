package com.vestra.product.service.repository;

import com.vestra.product.service.entity.Product;
import com.vestra.product.service.projection.ProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"variants","category","variants.attributes"})
    Page<ProductProjection> findAllProjectedBy(Pageable pageable);

    boolean existsBySlug(String slug);




}
