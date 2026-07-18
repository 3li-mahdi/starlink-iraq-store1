package com.starlinkiraq.store.dto.coupon;

import com.starlinkiraq.store.entity.Coupon;
import com.starlinkiraq.store.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        Integer maxUses,
        int currentUses,
        LocalDateTime expiresAt,
        boolean isActive
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getId(), coupon.getCode(), coupon.getDiscountType(),
                coupon.getDiscountValue(), coupon.getMinOrderAmount(), coupon.getMaxUses(),
                coupon.getCurrentUses(), coupon.getExpiresAt(), coupon.isActive());
    }
}
