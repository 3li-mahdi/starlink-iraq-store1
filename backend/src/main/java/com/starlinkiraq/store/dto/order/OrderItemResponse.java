package com.starlinkiraq.store.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        BigDecimal priceAtPurchase,
        java.util.List<String> digitalContent
) {
}
