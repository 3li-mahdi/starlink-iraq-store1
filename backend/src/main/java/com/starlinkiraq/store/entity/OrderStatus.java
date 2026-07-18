package com.starlinkiraq.store.entity;

/**
 * الحالات الممكنة لدورة حياة الطلب.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
