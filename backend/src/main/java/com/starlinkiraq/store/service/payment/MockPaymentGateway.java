package com.starlinkiraq.store.service.payment;

import com.starlinkiraq.store.entity.Order;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * بوابة دفع وهمية (Mock) تُنجح كل العمليات دائماً - تُستخدم لحين ربط بوابة دفع حقيقية (ZainCash) لاحقاً.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(Order order) {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        String reference = "MOCK-" + HexFormat.of().formatHex(bytes);
        return new PaymentResult(true, reference, "تمت عملية الدفع الوهمي بنجاح");
    }
}
