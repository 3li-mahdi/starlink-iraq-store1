package com.starlinkiraq.store.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * صيغة موحدة لأي قائمة مُرقّمة (paginated) تُرجَع من الـ API.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
