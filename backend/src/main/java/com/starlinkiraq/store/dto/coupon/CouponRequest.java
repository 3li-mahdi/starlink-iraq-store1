package com.starlinkiraq.store.dto.coupon;

import com.starlinkiraq.store.entity.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank(message = "كود الكوبون مطلوب")
        @Size(max = 50)
        String code,

        @NotNull(message = "نوع الخصم مطلوب")
        DiscountType discountType,

        @NotNull(message = "قيمة الخصم مطلوبة")
        @DecimalMin(value = "0.0", inclusive = false, message = "قيمة الخصم يجب أن تكون أكبر من صفر")
        BigDecimal discountValue,

        @PositiveOrZero
        BigDecimal minOrderAmount,

        Integer maxUses,

        LocalDateTime expiresAt,

        boolean isActive
) {
}
