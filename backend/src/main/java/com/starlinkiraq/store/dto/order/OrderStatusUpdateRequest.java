package com.starlinkiraq.store.dto.order;

import com.starlinkiraq.store.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "الحالة الجديدة للطلب مطلوبة")
        OrderStatus status
) {
}
