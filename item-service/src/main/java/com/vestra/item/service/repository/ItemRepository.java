package com.vestra.item.service.repository;

import com.vestra.item.service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    Optional<Item> findByVariantId(UUID variantId);
    boolean existsByVariantId(UUID variantId);
    void deleteByVariantId(UUID variantId);
}
