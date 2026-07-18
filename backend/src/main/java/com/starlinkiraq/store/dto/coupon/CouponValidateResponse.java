package com.starlinkiraq.store.dto.coupon;

import java.math.BigDecimal;

public record CouponValidateResponse(
        boolean valid,
        String message,
        BigDecimal discountAmount
) {
}
