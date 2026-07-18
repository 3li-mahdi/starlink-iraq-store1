package com.starlinkiraq.store.dto.product;

import com.starlinkiraq.store.entity.DigitalDeliveryType;
import com.starlinkiraq.store.entity.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "اسم المنتج مطلوب")
        @Size(max = 200)
        String name,

        @Size(max = 5000)
        String description,

        @NotNull(message = "السعر مطلوب")
        @DecimalMin(value = "0.0", inclusive = false, message = "السعر يجب أن يكون أكبر من صفر")
        BigDecimal price,

        @PositiveOrZero(message = "سعر الخصم يجب أن يكون صفراً أو أكبر")
        BigDecimal discountPrice,

        @Size(max = 500)
        String imageUrl,

        @NotNull(message = "نوع المنتج مطلوب")
        ProductType productType,

        @Min(value = 0, message = "الكمية لا يمكن أن تكون سالبة")
        Integer stockQuantity,

        boolean requiresShipping,

        DigitalDeliveryType digitalDeliveryType,

        @NotBlank(message = "فئة المنتج مطلوبة")
        @Size(max = 100)
        String category,

        boolean isActive
) {
}
