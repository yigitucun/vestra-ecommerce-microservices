package com.vestra.product.service.dto.product;

import com.vestra.product.service.dto.category.CategoryResponse;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String imageUrl,
        String slug,
        CategoryResponse category,
        List<VariantResponse> variants
) {}