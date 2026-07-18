package com.starlinkiraq.store.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "معرّف المنتج مطلوب")
        Long productId,

        @Min(value = 1, message = "الكمية يجب أن تكون 1 على الأقل")
        int quantity
) {
}
