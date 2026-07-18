package com.starlinkiraq.store.dto.wishlist;

import com.starlinkiraq.store.dto.product.ProductResponse;

import java.time.LocalDateTime;

public record WishlistResponse(
        Long id,
        ProductResponse product,
        LocalDateTime addedAt
) {
}
