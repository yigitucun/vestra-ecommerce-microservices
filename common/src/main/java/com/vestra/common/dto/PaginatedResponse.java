package com.vestra.common.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> content,
        PageInfo page
) {
    public record PageInfo(
            int size,
            int number,
            long totalElements,
            int totalPages
    ) {}
}