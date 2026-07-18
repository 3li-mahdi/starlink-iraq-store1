package com.starlinkiraq.store.dto.order;

import com.starlinkiraq.store.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        String shippingAddress,
        String couponCode,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
