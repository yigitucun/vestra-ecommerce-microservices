package com.vestra.product.service.repository;

import com.vestra.product.service.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VariantRepository extends JpaRepository<Variant, UUID> {
    boolean existsBySku(String sku);
}
