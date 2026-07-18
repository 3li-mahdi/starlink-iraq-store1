package com.starlinkiraq.store.service.payment;

import com.starlinkiraq.store.entity.Order;

/**
 * واجهة عامة لبوابات الدفع، تسمح بإضافة بوابات حقيقية (مثل ZainCash) لاحقاً دون تعديل منطق الـ checkout.
 */
public interface PaymentGateway {

    /**
     * ينفّذ عملية دفع لطلب معيّن.
     *
     * @param order الطلب المطلوب دفع قيمته
     * @return نتيجة عملية الدفع
     */
    PaymentResult charge(Order order);
}
