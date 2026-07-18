package com.starlinkiraq.store.dto.product;

import com.starlinkiraq.store.entity.DigitalDeliveryType;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        String imageUrl,
        ProductType productType,
        Integer stockQuantity,
        boolean requiresShipping,
        DigitalDeliveryType digitalDeliveryType,
        String category,
        boolean isActive,
        BigDecimal averageRating,
        boolean lowStock
) {
    public static ProductResponse from(Product product, int lowStockThreshold) {
        return new ProductResponse(
                product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getDiscountPrice(), product.getImageUrl(), product.getProductType(),
                product.getStockQuantity(), product.isRequiresShipping(), product.getDigitalDeliveryType(),
                product.getCategory(), product.isActive(), product.getAverageRating(),
                product.isLowStock(lowStockThreshold)
        );
    }
}
