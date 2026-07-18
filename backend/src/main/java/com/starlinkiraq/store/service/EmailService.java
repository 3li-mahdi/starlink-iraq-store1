package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * مسؤول عن إرسال كل رسائل البريد الإلكتروني للمستخدمين (تفعيل الحساب، تأكيد الطلب، تحديث الشحن، عودة المخزون).
 * الإرسال غير متزامن (async) حتى لا يؤخر استجابة الـ API الرئيسية.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    /**
     * يرسل رابط تفعيل الحساب للمستخدم الجديد بعد التسجيل.
     *
     * @param toEmail البريد الإلكتروني للمستقبل
     * @param token رمز التفعيل الفريد
     * تأثير جانبي: يرسل رسالة بريد إلكتروني فعلية (أو يسجّل فشل الإرسال دون إيقاف التطبيق)
     */
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = appProperties.getFrontend().getBaseUrl() + "/verify-email?token=" + token;
        send(toEmail, "تفعيل حسابك بمتجر Starlink العراق",
                "مرحباً بك، الرجاء الضغط على الرابط التالي لتفعيل حسابك:\n" + link);
    }

    /**
     * يرسل تأكيد استلام الطلب بعد إتمام عملية الشراء بنجاح.
     *
     * @param toEmail البريد الإلكتروني للمستقبل
     * @param orderId معرّف الطلب
     * تأثير جانبي: يرسل رسالة بريد إلكتروني فعلية
     */
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Long orderId) {
        send(toEmail, "تأكيد الطلب رقم #" + orderId,
                "شكراً لشرائك من متجر Starlink العراق. تم استلام طلبك رقم #" + orderId + " بنجاح.");
    }

    /**
     * يرسل إشعاراً للمستخدم عند تغيّر حالة شحن طلبه.
     *
     * @param toEmail البريد الإلكتروني للمستقبل
     * @param orderId معرّف الطلب
     * @param newStatus الحالة الجديدة للطلب
     * تأثير جانبي: يرسل رسالة بريد إلكتروني فعلية
     */
    @Async
    public void sendOrderStatusUpdateEmail(String toEmail, Long orderId, String newStatus) {
        send(toEmail, "تحديث حالة الطلب رقم #" + orderId,
                "تم تحديث حالة طلبك رقم #" + orderId + " إلى: " + newStatus);
    }

    /**
     * يرسل تنبيهاً للمستخدم عند عودة منتج من قائمة رغباته للمخزون.
     *
     * @param toEmail البريد الإلكتروني للمستقبل
     * @param productName اسم المنتج الذي عاد للمخزون
     * تأثير جانبي: يرسل رسالة بريد إلكتروني فعلية
     */
    @Async
    public void sendBackInStockEmail(String toEmail, String productName) {
        send(toEmail, "المنتج المفضل لديك عاد للمخزون",
                "المنتج \"" + productName + "\" المحفوظ بقائمة رغباتك أصبح متوفراً الآن بالمخزون.");
    }

    private void send(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("تعذّر إرسال البريد الإلكتروني إلى {}: {}", toEmail, e.getMessage());
        }
    }
}
