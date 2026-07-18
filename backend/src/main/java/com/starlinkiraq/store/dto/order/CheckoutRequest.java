package com.starlinkiraq.store.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "معرّف العملية الفريد مطلوب لمنع الدفع المكرر")
        @Size(max = 100)
        String idempotencyKey,

        @Size(max = 500)
        String shippingAddress,

        @Size(max = 50)
        String couponCode
) {
}
