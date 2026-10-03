package com.vestra.product.service.repository;

import com.vestra.product.service.entity.Category;
import com.vestra.product.service.projection.CategoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsBySlug(String slug);


    List<CategoryProjection> findAllBy();

}
