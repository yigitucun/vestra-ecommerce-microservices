package com.vestra.product.service.dto.category;

import com.vestra.product.service.annotation.UniqueCategorySlug;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record CreateCategoryRequest (
        @NotBlank String name,
        @UniqueCategorySlug @NotBlank String slug,
        String description,
        String parentId
) implements Serializable {}
