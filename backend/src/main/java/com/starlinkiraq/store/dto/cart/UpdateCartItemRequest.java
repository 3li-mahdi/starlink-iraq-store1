package com.starlinkiraq.store.dto.cart;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(value = 1, message = "الكمية يجب أن تكون 1 على الأقل")
        int quantity
) {
}
