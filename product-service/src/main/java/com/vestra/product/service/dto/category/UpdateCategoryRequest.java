package com.vestra.product.service.dto.category;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @NotBlank String slug,
        String description,
        String parentId
) { }
