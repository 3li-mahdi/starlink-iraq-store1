package com.starlinkiraq.store.dto.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        String userFullName,
        int rating,
        String comment,
        boolean isApproved,
        LocalDateTime createdAt
) {
}
