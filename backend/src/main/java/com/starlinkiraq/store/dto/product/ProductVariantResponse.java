package com.starlinkiraq.store.dto.product;

import com.starlinkiraq.store.entity.Product;

import java.math.BigDecimal;

/**
 * ملخّص مختصر لموديل ضمن مجموعة موديلات منتج واحد (يُستخدم لتعبئة قائمة الاختيار Dropdown).
 */
public record ProductVariantResponse(
        Long id,
        String variantLabel,
        BigDecimal price,
        BigDecimal discountPrice,
        Integer stockQuantity,
        boolean isActive
) {
    public static ProductVariantResponse from(Product product) {
        return new ProductVariantResponse(product.getId(), product.getVariantLabel(), product.getPrice(),
                product.getDiscountPrice(), product.getStockQuantity(), product.isActive());
    }
}
