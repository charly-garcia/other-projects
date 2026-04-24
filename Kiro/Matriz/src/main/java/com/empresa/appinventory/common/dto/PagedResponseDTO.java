package com.empresa.appinventory.common.dto;

import java.util.List;

/**
 * Generic paged response structure for list endpoints.
 * Wraps paginated content with metadata about the pagination state.
 *
 * @param <T> The type of content in the page
 */
public record PagedResponseDTO<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
}
