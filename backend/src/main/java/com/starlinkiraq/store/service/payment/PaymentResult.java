package com.starlinkiraq.store.service.payment;

/**
 * نتيجة محاولة تنفيذ عملية دفع عبر أي بوابة دفع.
 */
public record PaymentResult(boolean success, String transactionRef, String message) {
}
