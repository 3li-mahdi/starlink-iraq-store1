package com.starlinkiraq.store.dto.coupon;

import jakarta.validation.constraints.NotBlank;

public record CouponValidateRequest(
        @NotBlank(message = "كود الكوبون مطلوب")
        String code
) {
}
