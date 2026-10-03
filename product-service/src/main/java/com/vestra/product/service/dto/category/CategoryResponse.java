package com.vestra.product.service.dto.category;


import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description
) { }
